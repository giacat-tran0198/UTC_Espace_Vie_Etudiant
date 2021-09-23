import React from "react";
import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {Provider} from "react-redux";
import {createStore} from "redux";

import DiscussionSubmit from "./DiscussionSubmit";
import {authReducer} from "../redux/authReducer";
import * as apiCall from "../api/apiCall";

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
            <DiscussionSubmit/>
        </Provider>
    );
};

describe("DiscussionSubmit", () => {
    describe("Layout", () => {
        it("has textarea", () => {
            const {container} = setup();
            const textArea = container.querySelector("textarea");
            expect(textArea).toBeInTheDocument();
        });
        it("has image", () => {
            const {container} = setup();
            const image = container.querySelector("img");
            expect(image).toBeInTheDocument();
        });
        it("has textarea", () => {
            const {container} = setup();
            const textArea = container.querySelector("textarea");
            expect(textArea.rows).toBe(1);
        });
        it("displays user image", () => {
            const {container} = setup();
            const image = container.querySelector("img");
            expect(image.src).toContain("/images/profile/" + defaultState.image);
        });
    });
    describe("Interactions", () => {
        let textArea;
        const setupFocused = () => {
            const rendered = setup();
            textArea = rendered.container.querySelector("textarea");
            fireEvent.focus(textArea);
            return rendered;
        };

        it("displays 3 rows when focused to textarea", () => {
            setupFocused();
            expect(textArea.rows).toBe(3);
        });
        it("displays discussion button when focused to textarea", () => {
            const {queryByText} = setupFocused();
            const discussionButton = queryByText("Discussion");
            expect(discussionButton).toBeInTheDocument();
        });
        it("displays Cancel button when focused to textarea", () => {
            const {queryByText} = setupFocused();
            const cancelButton = queryByText("Cancel");
            expect(cancelButton).toBeInTheDocument();
        });
        it("does not display Discussion button when not focused to textarea", () => {
            const {queryByText} = setup();
            const discussionButton = queryByText("Discussion");
            expect(discussionButton).not.toBeInTheDocument();
        });
        it("does not display Cancel button when not focused to textarea", () => {
            const {queryByText} = setup();
            const cancelButton = queryByText("Cancel");
            expect(cancelButton).not.toBeInTheDocument();
        });
        it("returns back to unfocused state after clicking the cancel", () => {
            const {queryByText} = setupFocused();
            const cancelButton = queryByText("Cancel");
            fireEvent.click(cancelButton);
            expect(queryByText("Cancel")).not.toBeInTheDocument();
        });
        it("calls postDiscussion with discussion request object when clicking Discussion", () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            apiCall.postDiscussion = jest.fn().mockResolvedValue({});
            fireEvent.click(discussionButton);

            expect(apiCall.postDiscussion).toHaveBeenCalledWith({
                content: "Test discussion content"
            });
        });
        it("returns back to unfocused state after successful postDiscussion action", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            apiCall.postDiscussion = jest.fn().mockResolvedValue({});
            fireEvent.click(discussionButton);

            await waitFor(() => expect(queryByText("Discussion")).not.toBeInTheDocument());
        });
        it("clear content after successful postDiscussion action", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            apiCall.postDiscussion = jest.fn().mockResolvedValue({});
            fireEvent.click(discussionButton);

            await waitFor(() => expect(queryByText("Test discussion content")).not.toBeInTheDocument());
        });
        it("clears content after clicking cancel", () => {
            const {container, queryByText} = setup();
            const textArea = container.querySelector("textarea");
            fireEvent.focus(textArea);
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            fireEvent.click(queryByText("Cancel"));

            expect(queryByText("Test discussion content")).not.toBeInTheDocument();
        });
        it("disables Discussion button when there is postDiscussion api call", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            fireEvent.click(discussionButton);
            expect(mockFunction).toHaveBeenCalledTimes(1);
        });
        it("disables Cancel button when there is postDiscussion api call", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            const cancelButton = queryByText("Cancel");
            expect(cancelButton).toBeDisabled();
        });
        it("displays spinner when there is postDiscussion api call", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            expect(queryByText("Loading...")).toBeInTheDocument();
        });
        it("enables Discussion button when postDiscussion api call fails", async () => {
            const {container, queryByText} = setup();
            const textArea = container.querySelector("textarea");
            fireEvent.focus(textArea);
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockRejectedValueOnce({
                response: {
                    data: {
                        validationErrors: {
                            content: "It must have minimum 10 and maximum 5000 characters"
                        }
                    }
                }
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            await waitFor(() => expect(queryByText("Discussion")).not.toBeDisabled());
        });
        it("enables Cancel button when postDiscussion api call fails", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockRejectedValueOnce({
                response: {
                    data: {
                        validationErrors: {
                            content: "It must have minimum 10 and maximum 5000 characters"
                        }
                    }
                }
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            await waitFor(() => expect(queryByText("Cancel")).not.toBeDisabled());
        });
        it("enables Discussion button after successful postDiscussion action", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            apiCall.postDiscussion = jest.fn().mockResolvedValue({});
            fireEvent.click(discussionButton);

            await waitFor(() => {
                fireEvent.focus(textArea);
                expect(queryByText("Discussion")).not.toBeDisabled();
            });
        });
        it("displays validation error for content", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockRejectedValueOnce({
                response: {
                    data: {
                        validationErrors: {
                            content: "It must have minimum 10 and maximum 5000 characters"
                        }
                    }
                }
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            await waitFor(() => expect(
                queryByText("It must have minimum 10 and maximum 5000 characters")
            ).toBeInTheDocument());
        });
        it("clears validation error after clicking cancel", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockRejectedValueOnce({
                response: {
                    data: {
                        validationErrors: {
                            content: "It must have minimum 10 and maximum 5000 characters"
                        }
                    }
                }
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);


            await waitFor(() => {
                fireEvent.click(queryByText("Cancel"));
                expect(
                    queryByText("It must have minimum 10 and maximum 5000 characters")
                ).not.toBeInTheDocument();
            });
        });
        it("clears validation error after content is changed", async () => {
            const {queryByText} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const discussionButton = queryByText("Discussion");

            const mockFunction = jest.fn().mockRejectedValueOnce({
                response: {
                    data: {
                        validationErrors: {
                            content: "It must have minimum 10 and maximum 5000 characters"
                        }
                    }
                }
            });

            apiCall.postDiscussion = mockFunction;
            fireEvent.click(discussionButton);

            await waitFor(() => {
                fireEvent.change(textArea, {
                    target: {value: "Test discussion content updated"}
                });

                expect(
                    queryByText("It must have minimum 10 and maximum 5000 characters")
                ).not.toBeInTheDocument();
            });
        });
        it("displays file attachment input when text area focused", () => {
            const {container} = setup();
            const textArea = container.querySelector("textarea");
            fireEvent.focus(textArea);

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");
        });
        it("displays image component when file selected", async () => {
            apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
                data: {
                    id: 1,
                    name: "random-name.png"
                }
            });
            const {container} = setup();
            const textArea = container.querySelector("textarea");
            fireEvent.focus(textArea);

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });
            fireEvent.change(uploadInput, {target: {files: [file]}});
            await waitFor(() => {
                const images = container.querySelectorAll("img");
                const attachmentImage = images[1];
                expect(attachmentImage.src).toContain("data:image/png;base64");
            });
        });
        it("removes selected image after clicking cancel", async () => {
            apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
                data: {
                    id: 1,
                    name: "random-name.png"
                }
            });
            const {queryByText, container} = setupFocused();

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });
            fireEvent.change(uploadInput, {target: {files: [file]}});

            await waitFor(() => {
                fireEvent.click(queryByText("Cancel"));
                fireEvent.focus(textArea);
                const images = container.querySelectorAll("img");
                expect(images.length).toBe(1);
            });
        });
        it("calls postDiscussionFile when file selected", async () => {
            apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
                data: {
                    id: 1,
                    name: "random-name.png"
                }
            });

            const {container} = setupFocused();

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });
            fireEvent.change(uploadInput, {target: {files: [file]}});
            await waitFor(() => expect(apiCall.postDiscussionFile).toHaveBeenCalledTimes(1));
        });
        // it("calls postDiscussionFile with selected file", async (done) => {
        //     apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
        //         data: {
        //             id: 1,
        //             name: "random-name.png"
        //         }
        //     });
        //
        //     const {container} = setupFocused();
        //
        //     const uploadInput = container.querySelector("input");
        //     expect(uploadInput.type).toBe("file");
        //
        //     const file = new File(["dummy content"], "example.png", {
        //         type: "image/png"
        //     });
        //     fireEvent.change(uploadInput, {target: {files: [file]}});
        //     await waitFor(() => {
        //         const body = apiCall.postDiscussionFile.mock.calls[0][0];
        //         const reader = new FileReader();
        //         reader.onloadend = () => {
        //             expect(reader.result).toBe("dummy content");
        //             done();
        //         };
        //         reader.readAsText(body.get("file"));
        //     });
        // });
        // it("calls postDiscussion with discussion with file attachment object when clicking Discussion", async () => {
        //     apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
        //         data: {
        //             id: 1,
        //             name: "random-name.png"
        //         }
        //     });
        //     const {container} = setupFocused();
        //     fireEvent.change(textArea, {target: {value: "Test discussion content"}});
        //
        //     const uploadInput = container.querySelector("input");
        //     expect(uploadInput.type).toBe("file");
        //
        //     const file = new File(["dummy content"], "example.png", {
        //         type: "image/png"
        //     });
        //     fireEvent.change(uploadInput, {target: {files: [file]}});
        //
        //     const discussionButton = await screen.findByText("Discussion");
        //
        //     apiCall.postDiscussion = jest.fn().mockResolvedValue({});
        //     fireEvent.click(discussionButton);
        //
        //     expect(apiCall.postDiscussion).toHaveBeenCalledWith({
        //         content: "Test discussion content",
        //         attachment: {
        //             id: 1,
        //             name: "random-name.png"
        //         }
        //     });
        // });
        it("clears image after postDiscussion success", async () => {
            apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
                data: {
                    id: 1,
                    name: "random-name.png"
                }
            });
            const {container} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });
            fireEvent.change(uploadInput, {target: {files: [file]}});

            const discussionButton = await screen.findByText("Discussion");

            apiCall.postDiscussion = jest.fn().mockResolvedValue({});
            fireEvent.click(discussionButton);

            await waitFor(()=>{
                fireEvent.focus(textArea);
                const images = container.querySelectorAll("img");
                expect(images.length).toBe(1);
            });
        });
        it("calls postDiscussion without file attachment after cancelling previous file selection", async () => {
            apiCall.postDiscussionFile = jest.fn().mockResolvedValue({
                data: {
                    id: 1,
                    name: "random-name.png"
                }
            });
            const {queryByText, container} = setupFocused();
            fireEvent.change(textArea, {target: {value: "Test discussion content"}});

            const uploadInput = container.querySelector("input");
            expect(uploadInput.type).toBe("file");

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });
            fireEvent.change(uploadInput, {target: {files: [file]}});

            await waitFor(()=>{
                fireEvent.click(queryByText("Cancel"));
                fireEvent.focus(textArea);

                const discussionButton = queryByText("Discussion");

                apiCall.postDiscussion = jest.fn().mockResolvedValue({});
                fireEvent.change(textArea, {target: {value: "Test discussion content"}});
                fireEvent.click(discussionButton);

                expect(apiCall.postDiscussion).toHaveBeenCalledWith({
                    content: "Test discussion content"
                });
            });
        });
    });
});
console.error = () => {
};