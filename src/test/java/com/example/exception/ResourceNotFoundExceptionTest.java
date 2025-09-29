package com.example.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {
    @Test
    void testResourceNotFoundExceptionMessage() {
        String msg = "Resource not found!";
        ResourceNotFoundException ex = new ResourceNotFoundException(msg);
        assertThat(ex.getMessage()).isEqualTo(msg);
        assertThat(ex).isInstanceOf(ApiException.class);
    }
}
