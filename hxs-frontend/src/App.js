import React, { Suspense, lazy } from 'react';
import { BrowserRouter as Router, Switch, Route } from 'react-router-dom';

// 懒加载组件
const MobileLogin = lazy(() => import('./components/MobileLoginForm'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const UserProfile = lazy(() => import('./pages/UserProfile'));
import PrivateRoute from './components/PrivateRoute';
import CourseTablePage from './pages/CourseTablePage';
import ExecutePlanPage from './pages/ExecutePlanPage';
import AdminLoginPage from './pages/AdminLoginPage';
import AdminDashboard from './pages/AdminDashboard';
import AllScoresPage from './pages/AllScoresPage';
import GPACalculatorPage from './pages/GPACalculatorPage';
import EmptyClassroomPage from './pages/EmptyClassroomPage';
import FailRateRankPage from './pages/FailRateRankPage';
import TextbookQueryPage from './pages/TextbookQueryPage';
import NewCourseTablePage from './pages/NewCourseTablePage';
import SupportPage from './pages/SupportPage';

export default function App() {
  return (
    <Router>
      <Suspense fallback={<div>加载中...</div>}>
        <Switch>
          <Route exact path="/" component={MobileLogin} />
          <Route path="/support" component={SupportPage} />
          <PrivateRoute path="/dashboard" component={Dashboard} />
          <PrivateRoute path="/user-profile" component={UserProfile} />
          <PrivateRoute path="/course-table" component={CourseTablePage} />
          <PrivateRoute path="/execute-plan" component={ExecutePlanPage} />
          <Route path="/admin/login" component={AdminLoginPage} />
          <Route path="/admin/dashboard" component={AdminDashboard} />
          <PrivateRoute path="/all-scores" component={AllScoresPage} />
          <PrivateRoute path="/gpa-calculator" component={GPACalculatorPage} />
          <PrivateRoute path="/empty-classroom" component={EmptyClassroomPage} />
          <PrivateRoute path="/fail-rate-rank" component={FailRateRankPage} />
          <PrivateRoute path="/textbook-query" component={TextbookQueryPage} />
          <Route path="/new-course-table" component={NewCourseTablePage} />
        </Switch>
      </Suspense>
    </Router>
  );
}
