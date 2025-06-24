package Ecoshop.Exceptions;

public class ProductDoesNotExistsException extends Exception{
    public ProductDoesNotExistsException(String message) {
        super(message);
    }
}
