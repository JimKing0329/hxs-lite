// API 主机地址常量
export const API_HOST = 'http://localhost:8080';
// export const API_HOST = 'http://115.190.9.5:8080';
// export const API_HOST = 'http://115.190.9.5:8084';


// API 路径常量
export const API_PATHS = {

  // 管理员相关
  ADMIN_LOGIN: `${API_HOST}/admin/login`,
  GET_USER_DISTRIBUTION: `${API_HOST}/admin/getUserDistribution`,
  GET_USER_COUNT: `${API_HOST}/admin/getUserCount`,
  GET_TODAY_LOGIN_COUNT: `${API_HOST}/admin/getTodayLoginCount`,
  UPDATE_TERM_START_DATE: `${API_HOST}/admin/updateTermStartDate`,
  GET_CURRENT_TERM_START_DATE: `${API_HOST}/admin/getCurrentTermStartDate`,
  GET_SEVEN_DAY_LOGIN_COUNT: `${API_HOST}/admin/getSevenDayLoginCount`,
  UPDATE_MAJOR_INFO: `${API_HOST}/admin/updateMajorInfo`,
  UPDATE_EMPTY_CLASSROOM: `${API_HOST}/admin/updateEmptyClassroom`,
  UPDATE_WECHAT_MENU: `${API_HOST}/admin/updateMenu`,
  // 用户相关
  LOGIN: `${API_HOST}/user/login`,
  LOGOUT: `${API_HOST}/user/logout`,
  STUDENT_INFO: `${API_HOST}/user/student-info`,
  // 学习情况
  STUDY: {
    SITUATION: `${API_HOST}/study/situation`,
  },
  GET_EXECUTE_PLAN: `${API_HOST}/user/execute-plan`,
  UNBIND: `${API_HOST}/user/unbind`,
  UPDATE_MAJOR: `${API_HOST}/user/major-code`,

  // 课程相关
  TODAY_COURSE: `${API_HOST}/course/todayCourse`,
  TOMORROW_COURSE: `${API_HOST}/course/tomorrowCourse`,
  WEEK_COURSE: `${API_HOST}/course/weekCourse`,
  UPDATE_COURSE_TABLE: `${API_HOST}/course/updateCourseTable`,

  // 成绩相关（ScoreController）
  SCORE: {
    LIST: `${API_HOST}/scores`,
    DETAIL: `${API_HOST}/scores/detail`,
    FAIL_RATE_RANK: `${API_HOST}/scores/fail-rate-rank`,
    RANKING: `${API_HOST}/scores/ranking`,
    REFRESH: `${API_HOST}/scores`,
  },

  // 考试相关（ExamController）
  EXAM: {
    LIST: `${API_HOST}/exams`,
    REFRESH: `${API_HOST}/exams`,
  },

  // 空教室查询
  EMPTY_CLASSROOM: `${API_HOST}/emptyClassroom/getEmptyClassroom`,
  //教材查询
  GET_TEXT_BOOK: `${API_HOST}/textbooks`,  // 调用时追加 /{year}/{term}


};
