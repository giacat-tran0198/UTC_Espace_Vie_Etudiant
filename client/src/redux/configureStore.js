import {applyMiddleware, createStore} from "redux";
import {authReducer} from "./authReducer";
import logger from "redux-logger";
import thunk from "redux-thunk";
import * as apiCall from "../api/apiCall";


export const configureStore = (addLogger = true) => {
    let localStorageData = localStorage.getItem("blog-auth");

    let persistedState = {
        id: 0,
        username: "",
        displayName: "",
        image: "",
        password: "",
        isLoggedIn: false
    };
    if (localStorageData) {
        try {
            persistedState = JSON.parse(localStorageData);
            apiCall.setAuthorizationHeader(persistedState);
        } catch (error) {}
    }

    const middleware = addLogger
        ? applyMiddleware(thunk, logger)
        : applyMiddleware(thunk);

    const store = createStore(authReducer, persistedState, middleware);

    store.subscribe(() => {
        localStorage.setItem("blog-auth", JSON.stringify(store.getState()));
        apiCall.setAuthorizationHeader(store.getState());
    });

    return store;
};