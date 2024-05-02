package com.kcs.zolang.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cluster")
public class Cluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cluster_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "cluster_name", nullable = false)
    private String clusterName;

    @Column(name = "secret_token", nullable = false, columnDefinition = "TEXT")
    private String secretToken;

    @Column(name = "domain_url", nullable = false)
    private String domainUrl;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "cert_path", nullable = true)
    private String certPath;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Cluster(User user, String clusterName, String secretToken, String domainUrl, String version, String certPath) {
        this.user = user;
        this.clusterName = clusterName;
        this.secretToken = secretToken;
        this.domainUrl = domainUrl;
        this.version = version;
        this.certPath = certPath;
        this.createdAt = LocalDateTime.now();
    }
    public void update(String clusterName, String secretToken, String domainUrl, String version) {
        this.clusterName = clusterName;
        this.secretToken = secretToken;
        this.domainUrl = domainUrl;
        this.version = version;
    }
}
