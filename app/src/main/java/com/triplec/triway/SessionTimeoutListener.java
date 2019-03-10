package com.triplec.triway;

/**
 * interface used to check if the session has expired due to activity no response
 */
public interface SessionTimeoutListener {
    void onSessionTimeout();
}
