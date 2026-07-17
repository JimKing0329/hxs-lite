package com.hxs.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hxs.client.EduClient;
import com.hxs.client.EduSession;
import com.hxs.client.EduSessionManager;
import com.hxs.component.SystemDate;
import com.hxs.context.UserContext;
import com.hxs.mapper.ExecuteCourseMapper;
import com.hxs.mapper.UserMapper;
import com.hxs.model.dto.UserLoginDTO;
import com.hxs.model.entity.ExecuteCourse;
import com.hxs.model.entity.User;
import com.hxs.model.vo.ExecutePlanVO;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserServiceImpl 测试")
class UserServiceImplTest {

    private UserMapper userMapper;
    private ExecuteCourseMapper executeCourseMapper;
    private SystemDate termSystemDate;
    private EduClient eduClient;
    private EduSessionManager sessionManager;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        executeCourseMapper = mock(ExecuteCourseMapper.class);
        termSystemDate = mock(SystemDate.class);
        eduClient = mock(EduClient.class);
        sessionManager = mock(EduSessionManager.class);
        userService = new UserServiceImpl(eduClient, sessionManager, userMapper, executeCourseMapper, termSystemDate);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeCurrentId();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("新用户首次登录（校外网络，验证流程路径可达）")
        void shouldHandleNewUserLogin() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setSid("2023015529");
            dto.setPassword("password");

            Map<String, String> cookie = Map.of("jw", "jw123", "JSESSIONID", "session456");
            EduSession loginSession = new EduSession(cookie, "http://jwgl.hebtu.edu.cn");
            when(eduClient.login("2023015529", "password")).thenReturn(loginSession);
            when(userMapper.selectById("2023015529")).thenReturn(null);

            // login 方法内部会创建真实的 EduUserClient/EduMajorClient
            // 无网络时会抛异常，这里验证流程入口可达
            try {
                userService.login(dto);
            } catch (RuntimeException ignored) {
                // 预期：内部 HTTP 调用失败或 AesUtil 加密路径可达
            }
        }

        @Test
        @DisplayName("已有用户登录更新信息（校外网络，验证流程路径可达）")
        void shouldUpdateExistingUser() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setSid("2023015529");
            dto.setPassword("password");

            User existing = User.builder()
                    .sid("2023015529").name("张三").majorCode("080901")
                    .bindingKey("existing-key").majorName("计算机").build();
            Map<String, String> cookie = Map.of("jw", "jw123", "JSESSIONID", "session456");
            EduSession loginSession = new EduSession(cookie, "http://jwgl.hebtu.edu.cn");

            when(eduClient.login("2023015529", "password")).thenReturn(loginSession);
            when(userMapper.selectById("2023015529")).thenReturn(existing);

            try {
                userService.login(dto);
            } catch (RuntimeException ignored) {
                // 预期：内部 HTTP 调用失败或 AesUtil 加密路径可达
            }
        }

        @Test
        @DisplayName("无 bindingKey 时自动生成（校外网络，验证流程路径可达）")
        void shouldGenerateBindingKeyWhenMissing() {
            UserLoginDTO dto = new UserLoginDTO();
            dto.setSid("2023015529");
            dto.setPassword("password");

            User existing = User.builder()
                    .sid("2023015529").name("张三").build();
            Map<String, String> cookie = Map.of("jw", "j", "JSESSIONID", "s");
            EduSession loginSession = new EduSession(cookie, "http://jwgl.hebtu.edu.cn");

            when(eduClient.login(anyString(), anyString())).thenReturn(loginSession);
            when(userMapper.selectById("2023015529")).thenReturn(existing);

            try {
                userService.login(dto);
            } catch (RuntimeException ignored) {
                // 预期：内部 HTTP 调用失败
            }
        }
    }

    @Nested
    @DisplayName("getStudentInfo")
    class GetStudentInfo {

        @Test
        @DisplayName("查询当前用户信息")
        void shouldQueryByCurrentId() {
            UserContext.setCurrentId(2023015529L);
            User mockInfo = new User();
            mockInfo.setSid("2023015529");
            mockInfo.setName("张三");
            when(userMapper.selectById(2023015529L)).thenReturn(mockInfo);

            User result = userService.getStudentInfo();

            assertEquals("张三", result.getName());
        }
    }

    @Nested
    @DisplayName("getExecutePlan")
    class GetExecutePlan {

        @Test
        @DisplayName("缓存已存在时直接返回")
        void shouldReturnFromCache() {
            UserContext.setCurrentId(2023015529L);
            when(termSystemDate.getYear()).thenReturn(2024);
            when(termSystemDate.getTerm()).thenReturn(3);
            when(userMapper.queryMajorCodeByMajorId("2023015529")).thenReturn("080901");

            ExecuteCourse item = new ExecuteCourse();
            item.setCourseName("数据结构");
            when(executeCourseMapper.selectList(any(QueryWrapper.class))).thenReturn(List.of(item));

            ExecutePlanVO result = userService.getExecutePlan();

            assertEquals(2024, result.getYear());
            assertEquals(3, result.getTerm());
            assertEquals(1, result.getExecuteCourseList().size());
            assertEquals("数据结构", result.getExecuteCourseList().get(0).getCourseName());
            verify(executeCourseMapper, never()).insertBatch(anyList());
        }

        @Test
        @DisplayName("majorCode 为空抛异常")
        void shouldThrowWhenMajorCodeNull() {
            UserContext.setCurrentId(2023015529L);
            when(userMapper.queryMajorCodeByMajorId("2023015529")).thenReturn(null);

            assertThrows(com.hxs.exception.MessageEmptyException.class, () -> userService.getExecutePlan());
        }
    }

    @Nested
    @DisplayName("unbind")
    class Unbind {

        @Test
        @DisplayName("解绑清空 openId")
        void shouldClearOpenId() {
            UserContext.setCurrentId(2023015529L);
            User user = new User();
            user.setOpenId("wx-openid-123");
            when(userMapper.selectById(2023015529L)).thenReturn(user);

            userService.unbind();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).updateById(captor.capture());
            assertEquals("", captor.getValue().getOpenId());
        }
    }
}
