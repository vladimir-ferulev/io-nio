package com.company.nio.netty.httpserver2;

import static io.netty.handler.logging.LogLevel.INFO;

import java.security.cert.CertificateException;

import javax.net.ssl.SSLException;

import com.company.nio.netty.httpserver2.client.Http2ClientResponseHandler;
import com.company.nio.netty.httpserver2.client.Http2SettingsHandler;
import com.company.nio.netty.httpserver2.server.Http2ServerResponseHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpScheme;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandler;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class Http2Util {
    // SslContext используется как фабрика для SshHandler и настройки сервера/клиента для поддержки SSL/TLS
    public static SslContext createSSLContext(boolean isServer) throws SSLException, CertificateException {
        SslContext sslCtx;
        SelfSignedCertificate ssc = new SelfSignedCertificate();    // от Java 15 и новее возникает ошибка

        if (isServer) {
            sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                .sslProvider(SslProvider.JDK)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                    SelectorFailureBehavior.NO_ADVERTISE,
                    SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1))
                .build();
        } else {
            sslCtx = SslContextBuilder.forClient()
                .sslProvider(SslProvider.JDK)
                .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .applicationProtocolConfig(new ApplicationProtocolConfig(Protocol.ALPN,
                    SelectorFailureBehavior.NO_ADVERTISE,
                    SelectedListenerFailureBehavior.ACCEPT, ApplicationProtocolNames.HTTP_2))
                .build();
        }
        return sslCtx;

    }

    public static ApplicationProtocolNegotiationHandler getServerAPNHandler() {
        ApplicationProtocolNegotiationHandler serverAPNHandler = new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_2) {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) throws Exception {
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    ChannelPipeline pipeline = ctx.pipeline();
                    pipeline.addLast(Http2FrameCodecBuilder.forServer().build());   // Http2FrameCodec
                    pipeline.addLast(new Http2ServerResponseHandler());             // кастомный обработчик для исходящих и входящих данных
                    return;
                }
                throw new IllegalStateException("Protocol: " + protocol + " not supported");
            }
        };
        return serverAPNHandler;
    }

    public static ApplicationProtocolNegotiationHandler getClientAPNHandler(int maxContentLength, Http2SettingsHandler settingsHandler, Http2ClientResponseHandler responseHandler) {
        final Http2FrameLogger logger = new Http2FrameLogger(INFO, Http2Util.class);
        final Http2Connection connection = new DefaultHttp2Connection(false);

        HttpToHttp2ConnectionHandler connectionHandler = new HttpToHttp2ConnectionHandlerBuilder()
            .frameListener(new DelegatingDecompressorFrameListener(connection, new InboundHttp2ToHttpAdapterBuilder(connection).maxContentLength(maxContentLength)
            .propagateSettings(true)
            .build()))
            .frameLogger(logger)
            .connection(connection)
            .build();

        ApplicationProtocolNegotiationHandler clientAPNHandler = new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_2) {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                    ChannelPipeline pipeline = ctx.pipeline();
                    pipeline.addLast(connectionHandler);                    // для исходящих данных
                    pipeline.addLast(settingsHandler, responseHandler);     // для входящих данных
                    return;
                }
                ctx.close();
                throw new IllegalStateException("Protocol: " + protocol + " not supported");
            }
        };

        return clientAPNHandler;

    }

    public static FullHttpRequest createGetRequest(String host, int port) {
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.valueOf("HTTP/2.0"), HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);
        request.headers().add(HttpHeaderNames.HOST, new String(host + ":" + port));
        request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), HttpScheme.HTTPS);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
        request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
        return request;
    }
}
