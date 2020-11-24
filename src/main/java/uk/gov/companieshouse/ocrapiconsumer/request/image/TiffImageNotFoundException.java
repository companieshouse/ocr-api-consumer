package uk.gov.companieshouse.ocrapiconsumer.request.image;

/**
 * Thrown when an image is not found on CHIPS given the image endpoint passed through.
 */
public class TiffImageNotFoundException extends Exception {
    public TiffImageNotFoundException() {
        super();
    }
}
