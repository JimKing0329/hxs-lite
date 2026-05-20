import React from 'react';
import { Route, Redirect } from 'react-router-dom';
import { message } from 'antd';

const PrivateRoute = ({ component: Component, ...rest }) => {
  // Move hook calls to top level
  const isAuthenticated = localStorage.getItem('token');

  return (
    <Route
      {...rest}
      render={props =>
        isAuthenticated ? (
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