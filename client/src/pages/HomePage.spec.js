import React from "react";
import {render} from "@testing-library/react";
import {Provider} from "react-redux";
import {createStore} from "redux";

import HomePage from "./HomePage";
import * as apiCall from "../api/apiCall";
import {authReducer} from "../redux/authReducer";

const defaultState = {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "profile1.png",
    password: "P4ssword",
    isLoggedIn: true
};

let store;

const setup = (state = defaultState) => {
    store = createStore(authReducer, state);
    return render(
        <Provider store={store}>
            <HomePage/>
        </Provider>
    );
};

apiCall.listUsers = jest.fn().mockResolvedValue({
    data: {
        content: [],
        number: 0,
        size: 3
    }
});

apiCall.loadDiscussions = jest.fn().mockResolvedValue({
    data: {
        content: [],
        number: 0,
        size: 3
    }
});


describe("HomePage", () => {
    describe("Layout", () => {
        it("has root page div", () => {
            const {queryByTestId} = setup();
            const homePageDiv = queryByTestId("homepage");
            expect(homePageDiv).toBeInTheDocument();
        });
    });
    it("displays discussion submit when user logged in", () => {
        const {container} = setup();
        const textArea = container.querySelector("textarea");
        expect(textArea).toBeInTheDocument();
    });
    it("does not display discussion submit when user not logged in", () => {
        const notLoggedInState = {
            id: 0,
            username: "",
            displayName: "",
            password: "",
            image: "",
            isLoggedIn: false
        };
        const {container} = setup(notLoggedInState);
        const textArea = container.querySelector("textarea");
        expect(textArea).not.toBeInTheDocument();
    });
});

console.error = () => {
};