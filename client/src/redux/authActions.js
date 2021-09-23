import * as apiCall from "../api/apiCall";

export const loginSuccess = (loginUserData) => ({
    type: "login-success",
    payload: loginUserData
});

export const loginHandler = (credentials) => (dispatch) =>
    apiCall.login(credentials)
        .then((response) => {
            dispatch(
                loginSuccess({
                    ...response.data,
                    password: credentials.password
                })
            );
            return response;
        });

export const signupHandler = (user) => (dispatch) =>
    apiCall.signup(user)
        .then((response) => dispatch(loginHandler(user)));