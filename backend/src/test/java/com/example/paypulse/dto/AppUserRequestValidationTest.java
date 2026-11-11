package com.example.paypulse.dto;

import com.example.paypulse.model.UserRole;
import com.example.paypulse.model.UserStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserRequestValidationTest {

    private static final String NAME_51_CHARS = "a".repeat(51);
    private static final String EMAIL_255_CHARS = "a".repeat(243) + "@example.com";

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void validCreateRequestHasNoViolations() {
        assertThat(validator.validate(create("Iva", "Babic", "iva@example.com", "+385911234567", UserRole.USER)))
                .isEmpty();
    }

    @Test
    void phoneIsOptionalOnCreate() {
        assertThat(validator.validate(create("Iva", "Babic", "iva@example.com", null, UserRole.ADMIN))).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateRequests")
    void invalidCreateRequestIsRejectedOnTheRightField(String description, CreateAppUserRequest request,
                                                       String field) {
        assertThat(invalidFields(validator.validate(request))).containsExactly(field);
    }

    static Stream<Arguments> invalidCreateRequests() {
        return Stream.of(
                Arguments.of("missing first name", create(null, "Babic", "iva@example.com", null, UserRole.USER), "firstName"),
                Arguments.of("blank first name", create("  ", "Babic", "iva@example.com", null, UserRole.USER), "firstName"),
                Arguments.of("first name over 50", create(NAME_51_CHARS, "Babic", "iva@example.com", null, UserRole.USER), "firstName"),
                Arguments.of("missing last name", create("Iva", null, "iva@example.com", null, UserRole.USER), "lastName"),
                Arguments.of("last name over 50", create("Iva", NAME_51_CHARS, "iva@example.com", null, UserRole.USER), "lastName"),
                Arguments.of("missing email", create("Iva", "Babic", null, null, UserRole.USER), "email"),
                Arguments.of("email without @", create("Iva", "Babic", "iva.example.com", null, UserRole.USER), "email"),
                Arguments.of("email without dot in domain", create("Iva", "Babic", "iva@localhost", null, UserRole.USER), "email"),
                Arguments.of("email with spaces", create("Iva", "Babic", "iva babic@example.com", null, UserRole.USER), "email"),
                Arguments.of("email over 254", create("Iva", "Babic", EMAIL_255_CHARS, null, UserRole.USER), "email"),
                Arguments.of("phone without plus", create("Iva", "Babic", "iva@example.com", "0911234567", UserRole.USER), "phone"),
                Arguments.of("missing role", create("Iva", "Babic", "iva@example.com", null, null), "role"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+385911234567", "+12", "+123456789012345"})
    void e164PhoneNumbersAreAccepted(String phone) {
        assertThat(validator.validate(create("Iva", "Babic", "iva@example.com", phone, UserRole.USER))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"385911234567", "+0385911234567", "+1234567890123456", "+385 91 123 4567", "+38591abc", ""})
    void nonE164PhoneNumbersAreRejected(String phone) {
        assertThat(invalidFields(validator.validate(create("Iva", "Babic", "iva@example.com", phone, UserRole.USER))))
                .containsExactly("phone");
    }

    @Test
    void emptyUpdateRequestIsValidBecauseEveryFieldIsOptional() {
        assertThat(validator.validate(new UpdateAppUserRequest(null, null, null, null, null, null))).isEmpty();
    }

    @Test
    void fullUpdateRequestIsValid() {
        UpdateAppUserRequest request = new UpdateAppUserRequest("Iva", "Babic", "iva@example.com", "+385911234567",
                UserRole.ADMIN, UserStatus.INACTIVE);

        assertThat(validator.validate(request)).isEmpty();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidUpdateRequests")
    void invalidUpdateRequestIsRejectedOnTheRightField(String description, UpdateAppUserRequest request,
                                                       String field) {
        assertThat(invalidFields(validator.validate(request))).containsExactly(field);
    }

    static Stream<Arguments> invalidUpdateRequests() {
        return Stream.of(
                Arguments.of("blank first name", new UpdateAppUserRequest(" ", null, null, null, null, null), "firstName"),
                Arguments.of("first name over 50", new UpdateAppUserRequest(NAME_51_CHARS, null, null, null, null, null), "firstName"),
                Arguments.of("empty last name", new UpdateAppUserRequest(null, "", null, null, null, null), "lastName"),
                Arguments.of("blank email", new UpdateAppUserRequest(null, null, "   ", null, null, null), "email"),
                Arguments.of("malformed email", new UpdateAppUserRequest(null, null, "iva@", null, null, null), "email"),
                Arguments.of("malformed phone", new UpdateAppUserRequest(null, null, null, "091", null, null), "phone"));
    }

    private static CreateAppUserRequest create(String firstName, String lastName, String email, String phone,
                                               UserRole role) {
        return new CreateAppUserRequest(firstName, lastName, email, phone, role);
    }

    private static <T> Set<String> invalidFields(Set<ConstraintViolation<T>> violations) {
        return Set.copyOf(violations.stream().map(v -> v.getPropertyPath().toString()).toList());
    }
}
