package com.example.products.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExchangeRateResponse(
        @JsonProperty("broj_tecajnice") String brojTecajnice,
        @JsonProperty("datum_primjene") String datumPrimjene,
        @JsonProperty("valuta") String valuta,
        @JsonProperty("kupovni_tecaj") String kupovniTecaj,
        @JsonProperty("srednji_tecaj") String srednjiTecaj,
        @JsonProperty("prodajni_tecaj") String prodajniTecaj
) {}
