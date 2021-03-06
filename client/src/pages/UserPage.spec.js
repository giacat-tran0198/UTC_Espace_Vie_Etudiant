import React from "react";
import {Provider} from "react-redux";
import axios from "axios";
import {fireEvent, render, screen, waitFor, waitForDomChange, waitForElement} from "@testing-library/react";
import UserPage from "./UserPage";
import * as apiCall from "../api/apiCall";
import {configureStore} from "../redux/configureStore";

apiCall.loadDiscussions = jest.fn().mockResolvedValue({
    data: {
        content: [],
        number: 0,
        size: 3
    }
});

const mockSuccessGetUser = {
    data: {
        id: 1,
        username: "user1",
        displayName: "display1",
        image: "profile1.png"
    }
};

const mockFailUpdateUser = {
    response: {
        data: {
            validationErrors: {
                displayName: "It must have minimum 4 and maximum 255 characters",
                image: "Only PNG and JPG files are allowed"
            }
        }
    }
};

const match = {
    params: {
        username: "user1"
    }
};

const mockFailGetUser = {
    response: {
        data: {
            message: "User not found"
        }
    }
};

const mockSuccessUpdateUser = {
    data: {
        id: 1,
        username: "user1",
        displayName: "display1-update",
        image: "profile1-update.png"
    }
};

let store;

const setup = (props) => {
    store = configureStore(false);
    return render(
        <Provider store={store}>
            <UserPage {...props} />
        </Provider>
    );
};

beforeEach(() => {
    localStorage.clear();
    delete axios.defaults.headers.common["Authorization"];
});
const setUserOneLoggedInStorage = () => {
    localStorage.setItem(
        "blog-auth",
        JSON.stringify({
            id: 1,
            username: "user1",
            displayName: "display1",
            image: "profile1.png",
            password: "P4ssword",
            isLoggedIn: true
        })
    );
};


describe("UserPage", () => {
    describe("Layout", () => {
        it("has root page div", () => {
            const {queryByTestId} = setup();
            const homePageDiv = queryByTestId("userpage");
            expect(homePageDiv).toBeInTheDocument();
        });
        it("displays the displayName@username when user data loaded", async () => {
            apiCall.getUser = jest.fn().mockResolvedValue(mockSuccessGetUser);
            setup({match});
            const text = await screen.findByText("display1@user1");
            expect(text).toBeInTheDocument();
        });
        it("displays not found alert when user not found", async () => {
            apiCall.getUser = jest.fn().mockRejectedValue(mockFailGetUser);
            setup({match});
            const alert = await screen.findByText("User not found");
            expect(alert).toBeInTheDocument();
        });
        it("displays spinner while loading user data", () => {
            const mockDelayedResponse = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve(mockSuccessGetUser);
                    }, 300);
                });
            });
            apiCall.getUser = mockDelayedResponse;
            const { queryAllByText } = setup({ match });
            const spinners = queryAllByText('Loading...');
            expect(spinners.length).not.toBe(0);
        });
        it("displays the edit button when loggedInUser matches to user in url", async () => {
            setUserOneLoggedInStorage();
            apiCall.getUser = jest.fn().mockResolvedValue(mockSuccessGetUser);
            setup({match});
            const editButton = await screen.findByText("Edit");
            expect(editButton).toBeInTheDocument();
        });
    });
    describe("Lifecycle", () => {
        it("calls getUser when it is rendered", () => {
            apiCall.getUser = jest.fn().mockResolvedValue(mockSuccessGetUser);
            setup({match});
            expect(apiCall.getUser).toHaveBeenCalledTimes(1);
        });
        it("calls getUser for user1 when it is rendered with user1 in match", () => {
            apiCall.getUser = jest.fn().mockResolvedValue(mockSuccessGetUser);
            setup({match});
            expect(apiCall.getUser).toHaveBeenCalledWith("user1");
        });
    });
    describe("ProfileCard Interactions", () => {
        const setupForEdit = async () => {
            setUserOneLoggedInStorage();
            apiCall.getUser = jest.fn().mockResolvedValue(mockSuccessGetUser);
            const rendered = setup({match});
            const editButton = await screen.findByText("Edit");
            fireEvent.click(editButton);
            return rendered;
        };

        const mockDelayedUpdateSuccess = () =>
            jest.fn().mockImplementation(() =>
                new Promise((resolve, reject) =>
                    setTimeout(() => resolve(mockSuccessUpdateUser), 300)
                )
            );

        it("displays edit layout when clicking edit button", async () => {
            const {queryByText} = await setupForEdit();
            expect(queryByText("Save")).toBeInTheDocument();
        });
        it("returns back to none edit mode after clicking cancel", async () => {
            const {queryByText} = await setupForEdit();
            const cancelButton = queryByText("Cancel");
            fireEvent.click(cancelButton);
            expect(queryByText("Edit")).toBeInTheDocument();
        });
        it("calls updateUser api when clicking save", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            expect(apiCall.updateUser).toHaveBeenCalledTimes(1);
        });
        it("calls updateUser api with user id", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const userId = apiCall.updateUser.mock.calls[0][0];
            expect(userId).toBe(1);
        });
        it("calls updateUser api with request body having changed displayName", async () => {
            const {queryByText, container} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const requestBody = apiCall.updateUser.mock.calls[0][1];
            expect(requestBody.displayName).toBe("display1-update");
        });
        it("returns to non edit mode after successful updateUser api call", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const editButtonAfterClickingSave = await screen.findByText("Edit");
            expect(editButtonAfterClickingSave).toBeInTheDocument();
        });
        it("returns to original displayName after its changed in edit mode but cancelled", async () => {
            const {queryByText, container} = await setupForEdit();
            const displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            const cancelButton = queryByText("Cancel");
            fireEvent.click(cancelButton);
            const originalDisplayText = queryByText("display1@user1");
            expect(originalDisplayText).toBeInTheDocument();
        });
        it("returns to last updated displayName when display name is changed for another time but cancelled", async () => {
            const {queryByText, container} = await setupForEdit();
            let displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const editButtonAfterClickingSave = await screen.findByText("Edit");
            fireEvent.click(editButtonAfterClickingSave);
            displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {
                target: {value: "display1-update-second-time"}
            });
            const cancelButton = queryByText("Cancel");
            fireEvent.click(cancelButton);
            const lastSavedData = container.querySelector("h4");
            expect(lastSavedData).toHaveTextContent("display1-update@user1");
        });
        it("displays spinner when there is updateUser api call", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = mockDelayedUpdateSuccess();
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const spinner = queryByText("Loading...");
            expect(spinner).toBeInTheDocument();
        });
        it("disabels save button when there is updateUser api call", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = mockDelayedUpdateSuccess();
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            expect(saveButton).toBeDisabled();
        });

        it("disabels cancel button when there is updateUser api call", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = mockDelayedUpdateSuccess();
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const cancelButton = queryByText("Cancel");
            expect(cancelButton).toBeDisabled();
        });
        it("enables save button after updateUser api call success", async () => {
            const {queryByText, container} = await setupForEdit();
            let displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const editButtonAfterClickingSave = await screen.findByText("Edit");
            fireEvent.click(editButtonAfterClickingSave);
            const saveButtonAfterSecondEdit = queryByText("Save");
            expect(saveButtonAfterSecondEdit).not.toBeDisabled();
        });
        it("enables save button after updateUser api call fails", async () => {
            const {queryByText, container} = await setupForEdit();
            let displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            await waitFor(() => expect(saveButton).not.toBeDisabled());
        });
        it("displays the selected image in edit mode", async () => {
            const {container} = await setupForEdit();

            const inputs = container.querySelectorAll("input");
            const uploadInput = inputs[1];

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });

            fireEvent.change(uploadInput, {target: {files: [file]}});

            await waitFor(() => expect(container.querySelector("img").src).toContain("data:image/png;base64"))
        });
        it("returns back to the original image even the new image is added to upload box but cancelled", async () => {
            const {container} = await setupForEdit();

            const inputs = container.querySelectorAll("input");
            const uploadInput = inputs[1];

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });

            fireEvent.change(uploadInput, {target: {files: [file]}});

            const cancelButton = await screen.findByText("Cancel");
            fireEvent.click(cancelButton);

            const image = container.querySelector("img");
            expect(image.src).toContain("/images/profile/profile1.png");
        });
        it("does not throw error after file not selected", async () => {
            const {container} = await setupForEdit();
            const inputs = container.querySelectorAll("input");
            const uploadInput = inputs[1];
            expect(() =>
                fireEvent.change(uploadInput, {target: {files: []}})
            ).not.toThrow();
        });
        it("calls updateUser api with request body having new image without data:image/png;base64", async () => {
            const {queryByText, container} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);

            const inputs = container.querySelectorAll("input");
            const uploadInput = inputs[1];

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });

            await waitFor(() =>
                fireEvent.change(uploadInput, {
                    target: {files: [file]}
                })
            );

            const saveButton = await screen.findByText("Save");
            fireEvent.click(saveButton);

            const requestBody = apiCall.updateUser.mock.calls[0][1];

            expect(requestBody.image).not.toContain("data:image/png;base64");
        });

        it("returns to last updated image when image is change for another time but cancelled", async () => {
            const {queryByText, container} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);

            const inputs = container.querySelectorAll("input");
            const uploadInput = inputs[1];

            const file = new File(["dummy content"], "example.png", {
                type: "image/png"
            });

            fireEvent.change(uploadInput, {target: {files: [file]}});

            const saveButton = await screen.findByText("Save");
            fireEvent.click(saveButton);

            const editButtonAfterClickingSave = await screen.findByText("Edit");
            fireEvent.click(editButtonAfterClickingSave);

            const newFile = new File(["another content"], "example2.png", {
                type: "image/png"
            });

            fireEvent.change(uploadInput, {target: {files: [newFile]}});

            const cancelButton = queryByText("Cancel");
            fireEvent.click(cancelButton);
            const image = container.querySelector("img");
            expect(image.src).toContain("/images/profile/profile1-update.png");
        });
        it("displays validation error for displayName when update api fails", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const errorMessage = await screen.findByText(
                "It must have minimum 4 and maximum 255 characters"
            );
            expect(errorMessage).toBeInTheDocument();
        });
        it("shows validation error for file when update api fails", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);

            const errorMessage = await screen.findByText("Only PNG and JPG files are allowed");
            expect(errorMessage).toBeInTheDocument();
        });
        it("removes validation error for displayName when user changes the displayName", async () => {
            const {queryByText, container} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            // await waitForDomChange();
            const displayInput = container.querySelectorAll("input")[0];
            fireEvent.change(displayInput, {target: {value: "new-display-name"}});

            const errorMessage = queryByText(
                "It must have minimum 4 and maximum 255 characters"
            );
            await waitFor(() => expect(errorMessage).not.toBeInTheDocument());
        });
        it("removes validation error for file when user changes the file", async () => {
            const {queryByText, container} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);

            const fileInput = container.querySelectorAll("input")[1];

            const newFile = new File(["another content"], "example2.png", {
                type: "image/png"
            });
            await waitFor(() => fireEvent.change(fileInput, {target: {files: [newFile]}}));

            const errorMessage = queryByText("Only PNG and JPG files are allowed");
            await waitFor(() => expect(errorMessage).not.toBeInTheDocument());
        });
        it("removes validation error if user cancels", async () => {
            const {queryByText} = await setupForEdit();
            apiCall.updateUser = jest.fn().mockRejectedValue(mockFailUpdateUser);
            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            const cancel = await screen.findByText("Cancel");
            fireEvent.click(cancel);

            fireEvent.click(queryByText("Edit"));
            const errorMessage = queryByText(
                "It must have minimum 4 and maximum 255 characters"
            );
            expect(errorMessage).not.toBeInTheDocument();
        });
        it("updates redux state after updateUser api call success", async () => {
            const {queryByText, container} = await setupForEdit();
            let displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            await waitFor(() => {
                const storedUserData = store.getState();
                expect(storedUserData.displayName).toBe(mockSuccessUpdateUser.data.displayName);
                expect(storedUserData.image).toBe(mockSuccessUpdateUser.data.image);
            });
        });
        it("updates localStorage after updateUser api call success", async () => {
            const {queryByText, container} = await setupForEdit();
            let displayInput = container.querySelector("input");
            fireEvent.change(displayInput, {target: {value: "display1-update"}});
            apiCall.updateUser = jest.fn().mockResolvedValue(mockSuccessUpdateUser);

            const saveButton = queryByText("Save");
            fireEvent.click(saveButton);
            await waitFor(() => {
                const storedUserData = JSON.parse(localStorage.getItem("blog-auth"));
                expect(storedUserData.displayName).toBe(mockSuccessUpdateUser.data.displayName);
                expect(storedUserData.image).toBe(mockSuccessUpdateUser.data.image);
            });
        });
    });
});
console.error = () => {
};