package com.hxs.crontask;

import com.hxs.component.DateManager;
import com.hxs.service.user.EmptyClassroomService;
import com.hxs.task.EmptyClassroomTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.*;

/**
 * @author xin
 * @date 2026/7/11 12:55
 */
@ExtendWith(MockitoExtension.class)
public class TaskTest {

    @Mock
    private EmptyClassroomService emptyClassroomService;

    @Mock
    private DateManager dateManager;

    @InjectMocks
    private EmptyClassroomTask emptyClassroomTask;

    @Test
    void testUpdateEmptyClassroom() {
        // Arrange: term started 3 weeks ago → week should be 3 + 2 = 5
        LocalDate termStart = LocalDate.now().minusWeeks(3);
        when(dateManager.getTermStartDate()).thenReturn(termStart);

        // Act
        emptyClassroomTask.updateEmptyClassroom();

        // Assert
        verify(emptyClassroomService).updateEmptyClassRoom(5);
        verify(emptyClassroomService).deleteHistoryRecord();
    }
}
