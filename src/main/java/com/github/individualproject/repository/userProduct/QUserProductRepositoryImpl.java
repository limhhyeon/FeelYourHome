package com.github.individualproject.repository.userProduct;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QUserProductRepositoryImpl implements QUserProductRepository{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<String> findAllMqttTopics(Pageable pageable) {
        QUserProduct userProduct = QUserProduct.userProduct;

        List<String> mqttTopics = queryFactory.select(userProduct.mqttTopic)
                .from(userProduct)
                .where(userProduct.mqttTopic.isNotNull())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.select(userProduct.count())
                .from(userProduct)
                .where(userProduct.mqttTopic.isNotNull())
                .fetchOne();

        return new PageImpl<>(mqttTopics, pageable, total);
    }

    @Override
    public Page<String> findActiveMqttTopicsByActive(Pageable pageable) {
        QUserProduct userProduct = QUserProduct.userProduct;

        // MQTT topics where status is 'ACTIVE'
        List<String> mqttTopics = queryFactory.select(userProduct.mqttTopic)
                .from(userProduct)
                .where(userProduct.status.eq(Status.valueOf("ACTIVE")))
                .offset(pageable.getOffset())  // 페이지 오프셋
                .limit(pageable.getPageSize()) // 페이지 크기
                .fetch();

        // 총 데이터 수를 구함
        long total = queryFactory.select(userProduct.count())
                .from(userProduct)
                .where(userProduct.status.eq(Status.valueOf("ACTIVE")))
                .fetchOne();

        // Page 객체를 반환
        return new PageImpl<>(mqttTopics, pageable, total);
    }
}
