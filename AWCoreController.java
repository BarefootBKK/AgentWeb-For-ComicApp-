package com.example.comicapp.controllers;

import android.app.Activity;
import android.net.http.SslError;
import android.support.v4.app.Fragment;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.just.agentweb.AgentWeb;

public class AWCoreController {
    public static final String JS_INTERFACE_NAME = "local_obj";
    private boolean isActivity;
    private Activity mActivity;
    private Fragment mFragment;
    private AgentWeb agentWeb;
    private AgentWeb.PreAgentWeb preAgentWeb;
    private OnAgentDataLoadingListener dataLoadingListener;

    public AWCoreController(Activity mActivity, OnAgentDataLoadingListener dataLoadingListener) {
        this.mActivity = mActivity;
        this.dataLoadingListener = dataLoadingListener;
        this.isActivity = true;
        initWebView();
    }

    public AWCoreController(Fragment mFragment, OnAgentDataLoadingListener dataLoadingListener) {
        this.mFragment = mFragment;
        this.dataLoadingListener = dataLoadingListener;
        this.isActivity = false;
        initWebView();
    }

    public void loadUrl(String url) {
        if (agentWeb == null) {
            agentWeb = preAgentWeb.go(url);
        } else {
            agentWeb.getUrlLoader().loadUrl(url);
        }
    }

    public void destroy() {
        if (agentWeb != null) {
            agentWeb.getWebLifeCycle().onDestroy();
        }
    }

    public void pause() {
        if (agentWeb != null) {
            agentWeb.getWebLifeCycle().onPause();
        }
    }

    public void resume() {
        if (agentWeb != null) {
            agentWeb.getWebLifeCycle().onResume();
        }
    }

    public void stopLoading() {
        if (agentWeb != null) {
            agentWeb.getWebCreator().getWebView().stopLoading();
        }
    }

    public void loadUrlInCurrentWebView(String url) {
        if (agentWeb != null) {
            agentWeb.getUrlLoader().loadUrl(url);
        }
    }

    public void reload() {
        if (agentWeb != null) {
            agentWeb.getUrlLoader().reload();
        }
    }

    private void initWebView() {
        AgentWeb.AgentBuilder agentBuilder;
        FrameLayout frameLayout;
        if (isActivity) {
            agentBuilder = AgentWeb.with(mActivity);
            frameLayout = new FrameLayout(mActivity);
        } else {
            agentBuilder = AgentWeb.with(mFragment);
            frameLayout = new FrameLayout(mFragment.getContext());
        }
        preAgentWeb =
                agentBuilder
                .setAgentWebParent(frameLayout, new LinearLayout.LayoutParams(-1,-1))
                .useDefaultIndicator()
                .addJavascriptInterface(JS_INTERFACE_NAME, new MyJavaScriptInterface())
                .setWebViewClient(new MyWebViewClient())
                .setWebChromeClient(new MyWebChromeClient())
                .createAgentWeb()
                .ready();
    }

    public interface OnAgentDataLoadingListener {
        void onDataLoaded(String html);
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.loadUrl(getJavascriptUrl());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            view.loadUrl(getJavascriptUrl());
        }
    }

    private String getJavascriptUrl() {
        return "javascript:window." + JS_INTERFACE_NAME + ".showSource('<head>'+" +
                "document.getElementsByTagName('html')[0].innerHTML+'</head>');";
    }

    private final class MyJavaScriptInterface {
        @JavascriptInterface
        public void showSource(final String html) {
            dataLoadingListener.onDataLoaded(html);
        }
    }
}
