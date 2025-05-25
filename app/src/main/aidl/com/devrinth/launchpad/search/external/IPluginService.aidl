package com.devrinth.launchpad.search.external;

import com.devrinth.launchpad.search.external.IPluginCallback;
import com.devrinth.launchpad.search.external.PluginResponse;

interface IPluginService {
    void processQuery(String query, IPluginCallback callback);
}