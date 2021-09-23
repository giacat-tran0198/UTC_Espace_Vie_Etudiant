import React from "react";
import {render} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import DiscussionView from "./DiscussionView";
import {Provider} from "react-redux";
import {createStore} from "redux";
import {authReducer} from "../redux/authReducer";

const loggedInStateUser1 = {
    id: 1,
    username: "user1",
    displayName: "display1",
    image: "profile1.png",
    password: "P4ssword",
    isLoggedIn: true
};

const loggedInStateUser2 = {
    id: 2,
    username: "user2",
    displayName: "display2",
    image: "profile2.png",
    password: "P4ssword",
    isLoggedIn: true
};

const discussionWithoutAttachment = {
    id: 10,
    content: "This is the first discussion",
    user: {
        id: 1,
        username: "user1",
        displayName: "display1",
        image: "profile1.png"
    }
};

const discussionWithAttachment = {
    id: 10,
    content: "This is the first discussion",
    user: {
        id: 1,
        username: "user1",
        displayName: "display1",
        image: "profile1.png"
    },
    attachment: {
        fileType: "image/png",
        name: "attached-image.png"
    }
};

const discussionWithPdfAttachment = {
    id: 10,
    content: "This is the first discussion",
    user: {
        id: 1,
        username: "user1",
        displayName: "display1",
        image: "profile1.png"
    },
    attachment: {
        fileType: "application/pdf",
        name: "attached.pdf"
    }
};

const setup = (discussion = discussionWithoutAttachment, state = loggedInStateUser1) => {
    const oneMinute = 60 * 1000;
    const date = new Date(new Date() - oneMinute);

    discussion.date = date;
    const store = createStore(authReducer, state);

    return render(
        <Provider store={store}>
            <MemoryRouter>
                <DiscussionView discussion={discussion}/>
            </MemoryRouter>
        </Provider>
    );
};

describe("DiscussionView", () => {
    describe("Layout", () => {
        it("displays discussion content", () => {
            const {queryByText} = setup();
            expect(queryByText("This is the first discussion")).toBeInTheDocument();
        });
        it("displays users image", () => {
            const {container} = setup();
            const image = container.querySelector("img");
            expect(image.src).toContain("/images/profile/profile1.png");
        });
        it("displays displayName@user", () => {
            const {queryByText} = setup();
            expect(queryByText("display1@user1")).toBeInTheDocument();
        });
        it("displays relative time", () => {
            const {queryByText} = setup();
            expect(queryByText("1 minute ago")).toBeInTheDocument();
        });
        it("has link to user page", () => {
            const {container} = setup();
            const anchor = container.querySelector("a");
            expect(anchor.getAttribute("href")).toBe("/user1");
        });
        it("displays file attachment image", () => {
            const {container} = setup(discussionWithAttachment);
            const images = container.querySelectorAll("img");
            expect(images.length).toBe(2);
        });
        it("does not displays file attachment when attachment type is not image", () => {
            const {container} = setup(discussionWithPdfAttachment);
            const images = container.querySelectorAll("img");
            expect(images.length).toBe(1);
        });
        it("sets the attachment path as source for file attachment image", () => {
            const {container} = setup(discussionWithAttachment);
            const images = container.querySelectorAll("img");
            const attachmentImage = images[1];
            expect(attachmentImage.src).toContain(
                "/images/attachments/" + discussionWithAttachment.attachment.name
            );
        });
        it("displays delete button when discussion owned by logged in user", () => {
            const { container } = setup();
            expect(container.querySelector("button")).toBeInTheDocument();
        });
        it("does not display delete button when discussion is not owned by logged in user", () => {
            const { container } = setup(discussionWithoutAttachment, loggedInStateUser2);
            expect(container.querySelector("button")).not.toBeInTheDocument();
        });
    });
});