package com.kcs.zolang.service;

import com.kcs.zolang.domain.CICD;
import com.kcs.zolang.domain.Cluster;
import com.kcs.zolang.domain.User;
import com.kcs.zolang.dto.request.GitRepoRequestDto;
import com.kcs.zolang.exception.CommonException;
import com.kcs.zolang.exception.ErrorCode;
import com.kcs.zolang.repository.CICDRepository;
import com.kcs.zolang.repository.ClusterRepository;
import com.kcs.zolang.repository.UserRepository;
import com.kcs.zolang.utility.ClusterUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CICDService {
    private final CICDRepository cicdRepository;
    private final UserRepository userRepository;
    private final ClusterRepository clusterRepository;
    private final RestTemplate restTemplate;
    private final ClusterUtil clusterUtil;

    @Value("${github.webhook-url}")
    private String webhookUrl;

    @Transactional
    public void registerRepository(Long userId, GitRepoRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_USER));
        Cluster clusterProvidedByZolang = clusterRepository.findByProviderAndUserId("zolang", userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CLUSTER)); // 우선 클러스터 생성을 한 이후에만 배포 등록이 가능함

        String nickname = user.getNickname();
        String token = user.getGithubAccessToken();

        String apiUrl = String.format("https://api.github.com/repos/%s/%s/hooks", nickname, requestDto.repoName());
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("name", "web");
            body.put("active", true);
            body.put("events", Arrays.asList("push", "pull_request"));

            Map<String, String> config = new HashMap<>();
            config.put("url", webhookUrl);
            config.put("content_type", "json");

            body.put("config", config);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            headers.set("X-GitHub-Api-Version", "2022-11-28");
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(apiUrl, entity, String.class);

            CICD cicd = CICD.builder()
                    .user(user)
                    .repositoryName(requestDto.repoName())
                    .build();
            cicdRepository.save(cicd);
            clusterUtil.runPipeline(cicd, clusterProvidedByZolang, true);

        } catch (HttpClientErrorException e) {
            throw new CommonException(ErrorCode.FAILED_CREATE_WEBHOOK);
        }
    }
    @Transactional
    public void handleGithubWebhook(Map<String, Object> payload) {
        String repoName = (String) ((Map<String, Object>) payload.get("repository")).get("name");
        Long userId = cicdRepository.findByRepositoryName(repoName).get().getUser().getId(); // !!!!동일 리포지토리를 여러명이 등록한 경우 고려 아직 x
        Cluster clusterProvidedByZolang = clusterRepository.findByProviderAndUserId("zolang", userId)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_CLUSTER));
        CICD cicd = cicdRepository.findByRepositoryName(repoName)
                .orElseThrow(() -> new CommonException(ErrorCode.NOT_FOUND_REPOSITORY));
        clusterUtil.runPipeline(cicd,clusterProvidedByZolang, false);
    }
}
