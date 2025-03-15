package com.github.individualproject.web.advice;

import com.github.individualproject.service.exception.*;
import com.github.individualproject.web.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public ResponseDto handleNotFoundException(NotFoundException nfe){
        log.error("클라이언트 요청 이후 DB검색 중 발생한 에러입니다. " + nfe.getMessage());
        return new ResponseDto(HttpStatus.NOT_FOUND.value(),nfe.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ExceptionHandler(NotAcceptException.class)
    public ResponseDto handleNotAcceptException(NotAcceptException nae){
        log.error("클라이언트 요청 이후 DB검색 중 발생한 에러입니다. " + nae.getMessage());
        return new ResponseDto(HttpStatus.NOT_ACCEPTABLE.value(),nae.getMessage());
    }
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseDto handleAccessDeniedException(AccessDeniedException ade){
        log.error("Client 요청에 문제가 있어 다음처럼 출력합니다. " + ade.getMessage());
        return new ResponseDto(HttpStatus.FORBIDDEN.value(),ade.getMessage());
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(CAuthenticationEntryPointException.class)
    public ResponseDto handleAuthenticationException(CAuthenticationEntryPointException ae){
        log.error("Client 요청에 문제가 있어 다음처럼 출력합니다. " + ae.getMessage());
        return new ResponseDto(HttpStatus.UNAUTHORIZED.value(),ae.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadRequestException.class)
    public ResponseDto handleBadRequestException(BadRequestException bre){
        log.error("Client 요청에 문제가 있어 다음처럼 출력합니다. " + bre.getMessage());
        return new ResponseDto(HttpStatus.BAD_REQUEST.value(),bre.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto handleMethodValidationException(MethodArgumentNotValidException mve){
        String errorMessage =   mve.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("잘못된 요청입니다.");
        log.error("클라이언트 요청 이후 DB검색 중 발생한 에러입니다. " + errorMessage);
        return new ResponseDto(HttpStatus.BAD_REQUEST.value(),errorMessage);
    }
    @ExceptionHandler(TokenValidateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto handleTokenValidateException(TokenValidateException tve){
        log.error("클라이언트 요청 중 문제 발생 " + tve.getMessage());
        return new ResponseDto(HttpStatus.BAD_REQUEST.value(),tve.getMessage());
    }
}
