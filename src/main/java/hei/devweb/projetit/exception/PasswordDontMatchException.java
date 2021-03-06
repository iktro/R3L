package hei.devweb.projetit.exception;

import org.apache.logging.log4j.LogManager;

public class PasswordDontMatchException extends RuntimeException {

    static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    @Override
    public void printStackTrace() {
        LOGGER.error("Password don't match");
    }
}
