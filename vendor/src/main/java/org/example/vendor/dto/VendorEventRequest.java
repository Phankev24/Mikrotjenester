package org.example.vendor.dto;

/** Optional body for "vendor creates event". Anything missing is filled in from the vendor. */
public record VendorEventRequest(String name, String description, String location, Integer attendance) {}