package com.devrinth.launchpad.search.external;

import com.devrinth.launchpad.search.external.PluginResponse;

interface IPluginCallback {
    void onPluginResponse(in PluginResponse response);
}
