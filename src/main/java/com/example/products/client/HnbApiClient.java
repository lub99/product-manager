package com.example.products.client;

import com.example.products.client.dto.ExchangeRateResponse;
import com.example.products.exception.HnbApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
public class HnbApiClient {

    private static final ZoneId CET = ZoneId.of("Europe/Zagreb");

    private final RestClient hnbRestClient;

    public BigDecimal getUsdRate() {
        String today = LocalDate.now(CET).toString();

        ExchangeRateResponse[] rates = hnbRestClient.get()
                .uri("/tecajn-eur/v3?valuta=USD&datum-primjene={date}", today)
                .retrieve()
                .body(ExchangeRateResponse[].class);

        if (rates == null || rates.length == 0) {
            throw new HnbApiException("HNB API returned no exchange rate for USD");
        }
        ExchangeRateResponse latest = Arrays.stream(rates)
                .max(Comparator.comparingInt(r -> Integer.parseInt(r.brojTecajnice())))
                .orElseThrow(() -> new HnbApiException("HNB API returned no exchange rate for USD"));

        String normalized = latest.srednjiTecaj().replace(",", ".");
        return new BigDecimal(normalized);
    }
}
