package com.fiberfox.fxt;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;

/**
 * A helper class to create a OkHttp based channel.
 */
public class ChannelBuilder {
    public static ManagedChannel buildTls(String host, int port, InputStream caStream)
    {
        return build(host, port, null, true, caStream);
    }

    public static ManagedChannel buildTls(
            String host, int port, InputStream caStream, @Nullable String serverHostOverride)
    {
        return build(host, port, serverHostOverride, true, caStream);
    }

    public static ManagedChannel build(
            String host,
            int port,
            @Nullable String serverHostOverride,
            boolean useTls,
            @Nullable InputStream caStream) {
        ManagedChannelBuilder<?> channelBuilder = ManagedChannelBuilder
                .forAddress(host, port)
                .maxInboundMessageSize(16 * 1024 * 1024);
        if (serverHostOverride != null) {
            // Force the hostname to match the cert the server uses.
            channelBuilder.overrideAuthority(serverHostOverride);
        }
        if (useTls) {
            try {
                ((OkHttpChannelBuilder) channelBuilder).useTransportSecurity();
                ((OkHttpChannelBuilder) channelBuilder).sslSocketFactory(getSslSocketFactory(caStream));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            channelBuilder.usePlaintext();
        }
        return channelBuilder.build();
    }

    private static SSLSocketFactory getSslSocketFactory(@Nullable InputStream testCa)
            throws Exception {
        if (testCa == null) {
            return (SSLSocketFactory) SSLSocketFactory.getDefault();
        }

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, getTrustManagers(testCa) , null);
        return context.getSocketFactory();
    }

    private static TrustManager[] getTrustManagers(InputStream testCa) throws Exception {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(testCa);
        X500Principal principal = cert.getSubjectX500Principal();
        ks.setCertificateEntry(principal.getName("RFC2253"), cert);
        // Set up trust manager factory to use our key store.
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(ks);
        return trustManagerFactory.getTrustManagers();
    }
}