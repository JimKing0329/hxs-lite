import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { message } from 'antd';
import { isAuthenticated } from '../utils/request';

const PrivateRoute = ({ component: Component, ...rest }) => {
  return (
    <Route
      {...rest}
      render={props =>
        isAuthenticated() ? (
          <Component {...props} />
        ) : (
          <>
            {message.error('未登录，请先登录')}
            <Redirect to={{ pathname: "/", state: { from: props.location } }} />
          </>
        )
      }
    />
  );
};

export default PrivateRoute;
