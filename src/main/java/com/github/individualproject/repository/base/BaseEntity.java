package com.github.individualproject.repository.base;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
//이 어노테이션은 해당 클래스가 실제 엔티티가 아니라 다른 엔티티 클래스들이 상속받을 수 있는 부모 클래스임을 나타냅니다. BaseEntity 클래스는 데이터베이스 테이블로 직접 매핑되지 않고, 이를 상속받는 자식 엔티티들에게 공통 필드를 제공하는 역할을 합니다.
@MappedSuperclass
//이 어노테이션은 엔티티의 생명주기 이벤트를 감지하고 처리하는 리스너를 지정합니다25. AuditingEntityListener는 Spring Data JPA에서 제공하는 감사(auditing) 기능을 위한 리스너로, 엔티티의 생성 및 수정 시간을 자동으로 관리합니다
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
