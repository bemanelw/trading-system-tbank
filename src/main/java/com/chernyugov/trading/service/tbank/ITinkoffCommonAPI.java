package com.chernyugov.trading.service.tbank;

import ru.tinkoff.piapi.core.InvestApi;

public interface ITinkoffCommonAPI {
    InvestApi getApi();

    String getAccountId();

    boolean getIsSandboxMode();
}
