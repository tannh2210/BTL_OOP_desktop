package com.nhom5.eventticket.ui;

import com.nhom5.eventticket.AppContext;

/** Shared contract. All screen controllers receive the SAME AppContext. */
public interface ScreenController {
    void init(AppContext context, Navigator navigator);
}
