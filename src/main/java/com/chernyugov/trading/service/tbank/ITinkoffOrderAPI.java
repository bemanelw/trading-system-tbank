package com.chernyugov.trading.service.tbank;

import com.chernyugov.trading.service.dictionary.InstrumentService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

public interface ITinkoffOrderAPI {
    @AllArgsConstructor
    @Data
    @Builder
    class OrderResult {
        BigDecimal commission;
        BigDecimal price;
    }

    OrderResult buy(InstrumentService.Instrument instrument, BigDecimal price, Integer count);

    OrderResult sell(InstrumentService.Instrument instrument, BigDecimal price, Integer count);
}
