package org.example.vendor.dto;

/** Optional body for "vendor creates event". Missing fields get defaults from the vendor. */
public record VendorEventRequest(String name, String description, String location, Integer attendance) {}