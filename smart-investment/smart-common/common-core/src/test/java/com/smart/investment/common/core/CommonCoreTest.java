package com.smart.investment.common.core;

import com.smart.investment.common.core.constant.Constants;
import com.smart.investment.common.core.constant.RegexConstants;
import com.smart.investment.common.core.exception.BusinessException;
import com.smart.investment.common.core.exception.ErrorCode;
import com.smart.investment.common.core.exception.GlobalExceptionHandler;
import com.smart.investment.common.core.result.PageResult;
import com.smart.investment.common.core.result.Result;
import com.smart.investment.common.core.utils.DateTimeUtils;
import com.smart.investment.common.core.utils.JsonUtils;
import com.smart.investment.common.core.utils.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = TestApp.class)
@DisplayName("T-03 common-core 验收测试")
class CommonCoreTest {

    // ==================== 测试 Result<T> ====================

    @Nested
    @DisplayName("统一响应体 Result<T>")
    class ResultTest {

        @Test
        @DisplayName("Result.success(data) 返回格式与架构文档 7.1 节一致")
        void shouldReturnCorrectSuccessFormat() {
            Map<String, String> data = new HashMap<>();
            data.put("key", "value");

            Result<Map<String, String>> result = Result.success(data);

            assertEquals(200, result.getCode());
            assertEquals("操作成功", result.getMessage());
            assertNotNull(result.getData());
            assertEquals("value", result.getData().get("key"));
            assertNotNull(result.getTimestamp());
            assertTrue(result.getTimestamp() > 0);
        }

        @Test
        @DisplayName("Result.error 返回正确错误格式")
        void shouldReturnCorrectErrorFormat() {
            Result<Void> result = Result.error(4004, "资源不存在");

            assertEquals(4004, result.getCode());
            assertEquals("资源不存在", result.getMessage());
            assertNull(result.getData());
            assertNotNull(result.getTimestamp());
        }

        @Test
        @DisplayName("Result.success() 无数据成功响应")
        void shouldReturnSuccessWithoutData() {
            Result<Void> result = Result.success();
            assertEquals(200, result.getCode());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("Result.success(msg, data) 自定义消息成功响应")
        void shouldReturnSuccessWithCustomMessage() {
            Result<String> result = Result.success("创建成功", "id-001");
            assertEquals(200, result.getCode());
            assertEquals("创建成功", result.getMessage());
            assertEquals("id-001", result.getData());
        }
    }

    // ==================== 测试 PageResult<T> ====================

    @Nested
    @DisplayName("分页响应体 PageResult<T>")
    class PageResultTest {

        @Test
        @DisplayName("PageResult 支持泛型分页数据")
        void shouldSupportGenericPagination() {
            List<String> records = Arrays.asList("item1", "item2", "item3");
            PageResult<String> pageResult = PageResult.of(records, 100L, 1L, 20L);

            assertEquals(3, pageResult.getRecords().size());
            assertEquals(100L, pageResult.getTotal());
            assertEquals(1L, pageResult.getPage());
            assertEquals(20L, pageResult.getSize());
            assertEquals("item1", pageResult.getRecords().get(0));
        }

        @Test
        @DisplayName("PageResult.empty 返回空分页结果")
        void shouldReturnEmptyPageResult() {
            PageResult<Object> empty = PageResult.empty(1L, 20L);

            assertTrue(empty.getRecords().isEmpty());
            assertEquals(0L, empty.getTotal());
            assertEquals(1L, empty.getPage());
            assertEquals(20L, empty.getSize());
        }
    }

    // ==================== 测试异常体系 ====================

    @Nested
    @DisplayName("统一异常体系")
    class ExceptionTest {

        @Test
        @DisplayName("BusinessException 携带正确错误码")
        void shouldCarryCorrectErrorCode() {
            BusinessException ex = new BusinessException(ErrorCode.NOT_FOUND);

            assertEquals(ErrorCode.NOT_FOUND.getCode(), ex.getCode());
            assertEquals(ErrorCode.NOT_FOUND.getMessage(), ex.getMessage());
        }

        @Test
        @DisplayName("BusinessException 支持自定义消息")
        void shouldSupportCustomMessage() {
            BusinessException ex = new BusinessException(ErrorCode.BUSINESS_ERROR, "库存不足");

            assertEquals(ErrorCode.BUSINESS_ERROR.getCode(), ex.getCode());
            assertEquals("库存不足", ex.getMessage());
        }

        @Test
        @DisplayName("BusinessException 支持自定义错误码和消息")
        void shouldSupportCustomCodeAndMessage() {
            BusinessException ex = new BusinessException(9999, "自定义错误");

            assertEquals(9999, ex.getCode());
            assertEquals("自定义错误", ex.getMessage());
        }

        @Test
        @DisplayName("BusinessException 支持包装原始异常")
        void shouldWrapOriginalException() {
            RuntimeException cause = new RuntimeException("原始错误");
            BusinessException ex = new BusinessException(ErrorCode.INTERNAL_ERROR, cause);

            assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), ex.getCode());
            assertSame(cause, ex.getCause());
        }

        @Test
        @DisplayName("ErrorCode 枚举值不重复")
        void errorCodesShouldBeUnique() {
            ErrorCode[] codes = ErrorCode.values();
            long distinctCount = Arrays.stream(codes)
                    .map(ErrorCode::getCode)
                    .distinct()
                    .count();
            assertEquals(codes.length, distinctCount, "错误码不应重复");
        }
    }

    // ==================== 测试 GlobalExceptionHandler ====================

    @Nested
    @DisplayName("全局异常处理器")
    class GlobalExceptionHandlerTest {

        private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

        @Test
        @DisplayName("BusinessException 被正确捕获并返回统一格式")
        void shouldHandleBusinessException() {
            BusinessException ex = new BusinessException(ErrorCode.FORBIDDEN);

            Result<Void> result = handler.handleBusinessException(ex);

            assertEquals(ErrorCode.FORBIDDEN.getCode(), result.getCode());
            assertEquals(ErrorCode.FORBIDDEN.getMessage(), result.getMessage());
            assertNull(result.getData());
        }

        @Test
        @DisplayName("未知 Exception 返回通用 500 错误")
        void shouldHandleUnknownException() {
            Exception ex = new RuntimeException("未知错误");

            Result<Void> result = handler.handleException(ex);

            assertEquals(ErrorCode.INTERNAL_ERROR.getCode(), result.getCode());
            assertEquals(ErrorCode.INTERNAL_ERROR.getMessage(), result.getMessage());
        }
    }

    // ==================== 测试 JsonUtils ====================

    @Nested
    @DisplayName("JSON 工具类")
    class JsonUtilsTest {

        @Test
        @DisplayName("可正确序列化带 LocalDateTime 的对象")
        void shouldSerializeWithLocalDateTime() {
            TestDto dto = new TestDto();
            dto.setName("test");
            dto.setCreatedAt(LocalDateTime.of(2026, 6, 3, 10, 30, 0));

            String json = JsonUtils.toJsonString(dto);

            assertNotNull(json);
            assertTrue(json.contains("2026-06-03 10:30:00"));
        }

        @Test
        @DisplayName("可正确反序列化带 LocalDateTime 的对象")
        void shouldDeserializeWithLocalDateTime() {
            String json = "{\"name\":\"test\",\"createdAt\":\"2026-06-03 10:30:00\"}";

            TestDto dto = JsonUtils.parseObject(json, TestDto.class);

            assertEquals("test", dto.getName());
            assertNotNull(dto.getCreatedAt());
            assertEquals(2026, dto.getCreatedAt().getYear());
            assertEquals(6, dto.getCreatedAt().getMonthValue());
            assertEquals(3, dto.getCreatedAt().getDayOfMonth());
        }

        @Test
        @DisplayName("可序列化 List 为 JSON 数组")
        void shouldSerializeList() {
            List<String> list = Arrays.asList("a", "b", "c");
            String json = JsonUtils.toJsonString(list);
            assertEquals("[\"a\",\"b\",\"c\"]", json);
        }

        @Test
        @DisplayName("可反序列化 JSON 数组为 List")
        void shouldDeserializeList() {
            String json = "[\"a\",\"b\",\"c\"]";
            List<String> list = JsonUtils.parseArray(json, String.class);
            assertEquals(3, list.size());
            assertEquals("a", list.get(0));
        }

        @Test
        @DisplayName("toPrettyJsonString 输出格式化 JSON")
        void shouldOutputPrettyJson() {
            Map<String, Object> map = new HashMap<>();
            map.put("key", "value");
            String pretty = JsonUtils.toPrettyJsonString(map);
            assertTrue(pretty.contains("\n"));
        }

        static class TestDto {
            private String name;
            private LocalDateTime createdAt;

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }
            public LocalDateTime getCreatedAt() { return createdAt; }
            public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        }
    }

    // ==================== 测试 DateTimeUtils ====================

    @Nested
    @DisplayName("日期工具类")
    class DateTimeUtilsTest {

        @Test
        @DisplayName("format 和 parse 互为逆操作")
        void formatAndParseShouldBeReversible() {
            LocalDateTime now = LocalDateTime.of(2026, 6, 3, 14, 30, 0);
            String formatted = DateTimeUtils.format(now);
            LocalDateTime parsed = DateTimeUtils.parseDateTime(formatted);

            assertEquals(now, parsed);
        }

        @Test
        @DisplayName("时间戳与 LocalDateTime 互转正确")
        void timestampShouldBeCorrect() {
            LocalDateTime dt = LocalDateTime.of(2026, 6, 3, 0, 0, 0);
            long ts = DateTimeUtils.toTimestamp(dt);
            LocalDateTime back = DateTimeUtils.ofTimestamp(ts);

            assertEquals(dt, back);
        }

        @Test
        @DisplayName("format(LocalDate) 返回 yyyy-MM-dd")
        void shouldFormatLocalDate() {
            LocalDate date = LocalDate.of(2026, 6, 3);
            assertEquals("2026-06-03", DateTimeUtils.format(date));
        }

        @Test
        @DisplayName("format(LocalTime) 返回 HH:mm:ss")
        void shouldFormatLocalTime() {
            LocalTime time = LocalTime.of(14, 30, 0);
            assertEquals("14:30:00", DateTimeUtils.format(time));
        }

        @Test
        @DisplayName("daysAgo 计算正确")
        void daysAgoShouldBeCorrect() {
            LocalDate sevenDaysAgo = DateTimeUtils.daysAgo(7);
            assertEquals(LocalDate.now().minusDays(7), sevenDaysAgo);
        }

        @Test
        @DisplayName("isBetween 判断日期区间正确")
        void isBetweenShouldWork() {
            LocalDateTime target = LocalDateTime.of(2026, 6, 3, 12, 0);
            LocalDateTime start = LocalDateTime.of(2026, 6, 1, 0, 0);
            LocalDateTime end = LocalDateTime.of(2026, 6, 5, 0, 0);

            assertTrue(DateTimeUtils.isBetween(target, start, end));
            assertFalse(DateTimeUtils.isBetween(start.minusDays(1), start, end));
        }
    }

    // ==================== 测试 StringUtils ====================

    @Nested
    @DisplayName("字符串工具类")
    class StringUtilsTest {

        @Test
        @DisplayName("isEmpty / isNotEmpty 判断正确")
        void emptyCheckShouldWork() {
            assertTrue(StringUtils.isEmpty(null));
            assertTrue(StringUtils.isEmpty(""));
            assertFalse(StringUtils.isEmpty(" "));
            assertTrue(StringUtils.isNotEmpty("hello"));
        }

        @Test
        @DisplayName("isBlank / isNotBlank 判断正确")
        void blankCheckShouldWork() {
            assertTrue(StringUtils.isBlank(null));
            assertTrue(StringUtils.isBlank(""));
            assertTrue(StringUtils.isBlank("   "));
            assertFalse(StringUtils.isBlank("hello"));
        }

        @Test
        @DisplayName("camelToUnderline 转换正确")
        void camelToUnderlineShouldWork() {
            assertEquals("user_name", StringUtils.camelToUnderline("userName"));
            assertEquals("hello_world", StringUtils.camelToUnderline("helloWorld"));
        }

        @Test
        @DisplayName("underlineToCamel 转换正确")
        void underlineToCamelShouldWork() {
            assertEquals("userName", StringUtils.underlineToCamel("user_name"));
            assertEquals("helloWorld", StringUtils.underlineToCamel("hello_world"));
        }

        @Test
        @DisplayName("maskPhone 脱敏正确")
        void phoneMaskShouldWork() {
            assertEquals("138****5678", StringUtils.maskPhone("13812345678"));
        }

        @Test
        @DisplayName("maskEmail 脱敏正确")
        void emailMaskShouldWork() {
            assertEquals("t***@example.com", StringUtils.maskEmail("test@example.com"));
        }

        @Test
        @DisplayName("truncate 截断正确")
        void truncateShouldWork() {
            assertEquals("hello...", StringUtils.truncate("hello world", 5));
            assertEquals("hi", StringUtils.truncate("hi", 10));
        }

        @Test
        @DisplayName("matches 正则匹配正确")
        void matchesShouldWork() {
            assertTrue(StringUtils.matches("test@example.com", RegexConstants.EMAIL));
            assertFalse(StringUtils.matches("not-an-email", RegexConstants.EMAIL));
        }
    }

    // ==================== 测试常量类 ====================

    @Nested
    @DisplayName("常量类")
    class ConstantsTest {

        @Test
        @DisplayName("Constants 系统常量可访问")
        void constantsShouldBeAccessible() {
            assertEquals(1, Constants.DEFAULT_PAGE);
            assertEquals(20, Constants.DEFAULT_PAGE_SIZE);
            assertEquals(200, Constants.MAX_PAGE_SIZE);
            assertEquals(20L * 1024 * 1024, Constants.MAX_FILE_UPLOAD_SIZE);
            assertEquals("INVESTOR", Constants.ROLE_INVESTOR);
            assertEquals("ANALYST", Constants.ROLE_ANALYST);
            assertEquals("ADMIN", Constants.ROLE_ADMIN);
        }

        @Test
        @DisplayName("RegexConstants 正则常量可访问且有效")
        void regexConstantsShouldBeValid() {
            assertTrue("test_user".matches(RegexConstants.USERNAME));
            assertTrue("Pass1234".matches(RegexConstants.PASSWORD));
            assertTrue("13800138000".matches(RegexConstants.PHONE));
            assertTrue("test@example.com".matches(RegexConstants.EMAIL));
            assertTrue("000001".matches(RegexConstants.STOCK_CODE));
            assertTrue("2026-06-03".matches(RegexConstants.DATE));
            assertTrue("2026-06-03 14:30:00".matches(RegexConstants.DATE_TIME));
            assertTrue("report.pdf".matches(RegexConstants.FILE_NAME));
        }
    }

    // ==================== 测试应用配置 ====================

    @TestConfiguration
    static class TestConfig {
        @Bean
        GlobalExceptionHandler globalExceptionHandler() {
            return new GlobalExceptionHandler();
        }
    }

    @RestController
    static class TestController {
    }
}
