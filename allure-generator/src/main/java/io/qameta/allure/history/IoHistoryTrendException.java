package io.qameta.allure.history;

import java.io.IOException;

public class IoHistoryTrendException extends IOException {

    private static final long serialVersionUID = 7923759338218850553L;

    public IoHistoryTrendException(final Throwable cause) {
        super(cause);
    }

    public IoHistoryTrendException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
