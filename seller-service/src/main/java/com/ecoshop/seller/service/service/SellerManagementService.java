package com.ecoshop.seller.service.service;

import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.seller.service.domain.*;
import com.ecoshop.seller.service.dto.SellerDtos.*;
import com.ecoshop.seller.service.repo.PayoutRepository;
import com.ecoshop.seller.service.repo.SellerProductLinkRepository;
import com.ecoshop.seller.service.repo.SellerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SellerManagementService {

    private static final Logger log = LoggerFactory.getLogger(SellerManagementService.class);
    public static final String TOPIC_SELLER_VERIFIED = "seller.verified";
    public static final String TOPIC_SELLER_PRODUCT_APPROVED = "seller.product.approved";

    private final SellerRepository sellerRepository;
    private final SellerProductLinkRepository linkRepository;
    private final PayoutRepository payoutRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public SellerResponse onboard(UUID userId, OnboardSellerRequest req) {
        if (sellerRepository.findByUserId(userId).isPresent()) {
            throw BusinessException.conflict("SELLER_ALREADY_EXISTS",
                    "Seller account already exists for this user");
        }
        if (req.gstin() != null && sellerRepository.existsByGstin(req.gstin())) {
            throw BusinessException.conflict("GSTIN_TAKEN",
                    "GSTIN " + req.gstin() + " already registered");
        }
        Seller s = Seller.builder()
                .userId(userId)
                .displayName(req.displayName())
                .legalName(req.legalName())
                .contactEmail(req.contactEmail())
                .contactPhone(req.contactPhone())
                .gstin(req.gstin())
                .pan(req.pan())
                .status(SellerStatus.PENDING_VERIFICATION)
                .commissionRate(new BigDecimal("0.1000"))
                .bankAccountHolder(req.bankAccountHolder())
                .bankAccountLast4(req.bankAccountLast4())
                .bankIfsc(req.bankIfsc())
                .build();
        s = sellerRepository.save(s);
        log.info("Seller {} onboarded for user {} (status PENDING_VERIFICATION)", s.getId(), userId);
        return toResponse(s);
    }

    @Transactional
    public SellerResponse verify(UUID sellerId, VerifySellerRequest req, String actor) {
        Seller s = findSeller(sellerId);
        if (req.approve()) {
            s.setStatus(SellerStatus.ACTIVE);
            if (req.commissionRate() != null) s.setCommissionRate(req.commissionRate());
            log.info("Seller {} verified and activated by {}", sellerId, actor);
            publishVerified(s, true);
        } else {
            s.setStatus(SellerStatus.DEACTIVATED);
            s.setRejectionReason(req.reason());
            log.info("Seller {} rejected by {}: {}", sellerId, actor, req.reason());
            publishVerified(s, false);
        }
        return toResponse(s);
    }

    @Transactional
    public SellerResponse suspend(UUID sellerId, String reason, String actor) {
        Seller s = findSeller(sellerId);
        s.setStatus(SellerStatus.SUSPENDED);
        s.setRejectionReason(reason);
        log.info("Seller {} suspended by {}: {}", sellerId, actor, reason);
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public SellerResponse getMyAccount(UUID userId) {
        Seller s = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("SELLER_NOT_FOUND",
                        "No seller account for current user"));
        return toResponse(s);
    }

    @Transactional(readOnly = true)
    public SellerResponse getById(UUID sellerId) {
        return toResponse(findSeller(sellerId));
    }

    @Transactional(readOnly = true)
    public PageResponse<SellerResponse> listByStatus(SellerStatus status, int page, int size) {
        Page<Seller> result = sellerRepository.findByStatus(status, PageRequest.of(page, size));
        List<SellerResponse> content = result.getContent().stream().map(this::toResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    // ─── Product approvals ───

    @Transactional
    public ProductLinkResponse submitProduct(UUID sellerUserId, SubmitProductRequest req) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> BusinessException.notFound("SELLER_NOT_FOUND",
                        "Seller account required"));
        if (seller.getStatus() != SellerStatus.ACTIVE) {
            throw BusinessException.badRequest("SELLER_NOT_ACTIVE",
                    "Seller must be ACTIVE to submit products (current: " + seller.getStatus() + ")");
        }
        if (linkRepository.findByProductId(req.productId()).isPresent()) {
            throw BusinessException.conflict("PRODUCT_ALREADY_LINKED",
                    "Product already linked to a seller");
        }
        SellerProductLink link = SellerProductLink.builder()
                .sellerId(seller.getId())
                .productId(req.productId())
                .approvalStatus("PENDING")
                .build();
        link = linkRepository.save(link);
        return toLinkResponse(link);
    }

    @Transactional
    public ProductLinkResponse approveProduct(UUID linkId, ApproveProductRequest req, String actor) {
        SellerProductLink link = linkRepository.findById(linkId)
                .orElseThrow(() -> BusinessException.notFound("LINK_NOT_FOUND", "Link not found"));
        link.setApprovalStatus(req.approve() ? "APPROVED" : "REJECTED");
        link.setApprovalNote(req.note());
        log.info("Product link {} {} by {}", linkId, link.getApprovalStatus(), actor);

        if (req.approve()) {
            Map<String, Object> evt = new HashMap<>();
            evt.put("eventId", UUID.randomUUID().toString());
            evt.put("sellerId", link.getSellerId().toString());
            evt.put("productId", link.getProductId().toString());
            kafkaTemplate.send(TOPIC_SELLER_PRODUCT_APPROVED, link.getProductId().toString(), evt);
        }
        return toLinkResponse(link);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductLinkResponse> listMyProducts(UUID sellerUserId, int page, int size) {
        Seller seller = sellerRepository.findByUserId(sellerUserId)
                .orElseThrow(() -> BusinessException.notFound("SELLER_NOT_FOUND", "Seller not found"));
        Page<SellerProductLink> result = linkRepository.findBySellerId(
                seller.getId(), PageRequest.of(page, size));
        List<ProductLinkResponse> content = result.getContent().stream().map(this::toLinkResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductLinkResponse> listPendingApprovals(int page, int size) {
        Page<SellerProductLink> result = linkRepository.findByApprovalStatus(
                "PENDING", PageRequest.of(page, size));
        List<ProductLinkResponse> content = result.getContent().stream().map(this::toLinkResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    // ─── Payouts ───

    @Transactional
    public PayoutResponse createPayout(PayoutLineRequest req) {
        Seller seller = findSeller(req.sellerId());
        BigDecimal commission = req.grossSales().multiply(seller.getCommissionRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal refunds = req.refunds() != null ? req.refunds() : BigDecimal.ZERO;
        BigDecimal net = req.grossSales().subtract(commission).subtract(refunds)
                .setScale(2, RoundingMode.HALF_UP);
        Payout payout = Payout.builder()
                .sellerId(req.sellerId())
                .periodStart(req.periodStart())
                .periodEnd(req.periodEnd())
                .grossSales(req.grossSales())
                .commission(commission)
                .refunds(refunds)
                .netPayout(net)
                .currency("INR")
                .status(PayoutStatus.PENDING)
                .build();
        payout = payoutRepository.save(payout);
        log.info("Payout {} created for seller {} period {}-{}: gross={} commission={} net={}",
                payout.getId(), req.sellerId(), req.periodStart(), req.periodEnd(),
                req.grossSales(), commission, net);
        return toPayoutResponse(payout);
    }

    @Transactional
    public PayoutResponse markPayoutSettled(UUID payoutId, String externalRef) {
        Payout p = payoutRepository.findById(payoutId)
                .orElseThrow(() -> BusinessException.notFound("PAYOUT_NOT_FOUND", "Payout not found"));
        if (p.getStatus() != PayoutStatus.PROCESSING && p.getStatus() != PayoutStatus.PENDING) {
            throw BusinessException.badRequest("INVALID_STATE",
                    "Only PENDING/PROCESSING payouts can be settled (current: " + p.getStatus() + ")");
        }
        p.setStatus(PayoutStatus.COMPLETED);
        p.setSettledAt(Instant.now());
        p.setExternalRef(externalRef);
        return toPayoutResponse(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<PayoutResponse> listPayoutsForSeller(UUID sellerId, int page, int size) {
        Page<Payout> result = payoutRepository.findBySellerIdOrderByPeriodEndDesc(
                sellerId, PageRequest.of(page, size));
        List<PayoutResponse> content = result.getContent().stream().map(this::toPayoutResponse).toList();
        return new PageResponse<>(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    private Seller findSeller(UUID id) {
        return sellerRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("SELLER_NOT_FOUND",
                        "Seller " + id + " not found"));
    }

    private void publishVerified(Seller s, boolean approved) {
        Map<String, Object> evt = new HashMap<>();
        evt.put("eventId", UUID.randomUUID().toString());
        evt.put("sellerId", s.getId().toString());
        evt.put("userId", s.getUserId().toString());
        evt.put("approved", approved);
        evt.put("status", s.getStatus().name());
        kafkaTemplate.send(TOPIC_SELLER_VERIFIED, s.getId().toString(), evt);
    }

    public SellerResponse toResponse(Seller s) {
        return new SellerResponse(
                s.getId(), s.getUserId(), s.getDisplayName(), s.getLegalName(),
                s.getContactEmail(), s.getContactPhone(),
                s.getGstin(), s.getPan(),
                s.getStatus(), s.getCommissionRate(),
                s.getBankAccountHolder(), s.getBankAccountLast4(), s.getBankIfsc(),
                s.getRejectionReason(), s.getCreatedAt()
        );
    }

    public ProductLinkResponse toLinkResponse(SellerProductLink l) {
        return new ProductLinkResponse(
                l.getId(), l.getSellerId(), l.getProductId(),
                l.getApprovalStatus(), l.getApprovalNote()
        );
    }

    public PayoutResponse toPayoutResponse(Payout p) {
        return new PayoutResponse(
                p.getId(), p.getSellerId(),
                p.getPeriodStart(), p.getPeriodEnd(),
                p.getGrossSales(), p.getCommission(), p.getRefunds(), p.getNetPayout(),
                p.getCurrency(), p.getStatus(),
                p.getSettledAt(), p.getExternalRef()
        );
    }
}
