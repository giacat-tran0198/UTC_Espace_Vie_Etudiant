import React from "react";
import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from 'react-router-dom';
import UserList from "./UserList";
import * as apiCall from "../api/apiCall";

apiCall.listUsers = jest.fn().mockResolvedValue({
    data: {
        content: [],
        number: 0,
        size: 3
    }
});

const setup = () => {
    return render(
        <MemoryRouter>
            <UserList/>
        </MemoryRouter>
    );
};

const mockedEmptySuccessResponse = {
    data: {
        content: [],
        number: 0,
        size: 3
    }
};

const mockSuccessGetSinglePage = {
    data: {
        content: [
            {
                username: "user1",
                displayName: "display1",
                image: ""
            },
            {
                username: "user2",
                displayName: "display2",
                image: ""
            },
            {
                username: "user3",
                displayName: "display3",
                image: ""
            }
        ],
        number: 0,
        first: true,
        last: true,
        size: 3,
        totalPages: 1
    }
};

const mockSuccessGetMultiPageFirst = {
    data: {
        content: [
            {
                username: "user1",
                displayName: "display1",
                image: ""
            },
            {
                username: "user2",
                displayName: "display2",
                image: ""
            },
            {
                username: "user3",
                displayName: "display3",
                image: ""
            }
        ],
        number: 0,
        first: true,
        last: false,
        size: 3,
        totalPages: 2
    }
};

const mockSuccessGetMultiPageLast = {
    data: {
        content: [
            {
                username: "user4",
                displayName: "display4",
                image: ""
            }
        ],
        number: 1,
        first: false,
        last: true,
        size: 3,
        totalPages: 2
    }
};


const mockFailGet = {
    response: {
        data: {
            message: "Load error"
        }
    }
};

describe("UserList", () => {
    describe("Layout", () => {
        it("has header of Users", () => {
            const {container} = setup();
            const header = container.querySelector("h3");
            expect(header).toHaveTextContent("Users");
        });
        it("displays three items when listUser api returns three users", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValue(mockSuccessGetSinglePage);
            const {queryByTestId} = setup();
            await waitFor(() => expect(queryByTestId("usergroup").childElementCount).toBe(3))
        });
        it("displays the displayName@username when listUser api returns users", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValue(mockSuccessGetSinglePage);
            setup();
            const firstUser = await screen.findByText("display1@user1");
            expect(firstUser).toBeInTheDocument();
        });
        it('has link to UserPage', async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValue(mockSuccessGetSinglePage);
            const { container } = setup();
            await screen.findByText('display1@user1');
            const firstAnchor = container.querySelectorAll('a')[0];
            expect(firstAnchor.getAttribute('href')).toBe('/user1');
        });
    });
    describe("Lifecycle", () => {
        it("calls listUsers api when it is rendered", () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValue(mockedEmptySuccessResponse);
            setup();
            expect(apiCall.listUsers).toHaveBeenCalledTimes(1);
        });
        it("calls listUsers method with page zero and size three", () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValue(mockedEmptySuccessResponse);
            setup();
            expect(apiCall.listUsers).toHaveBeenCalledWith({page: 0, size: 3});
        });
    });
    describe("Interactions", () => {
        it("loads next page when clicked to next button", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst)
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast);
            setup();
            const nextLink = await screen.findByText("next >");
            fireEvent.click(nextLink);
            const secondPageUser = await screen.findByText("display4@user4");
            expect(secondPageUser).toBeInTheDocument();
        });
        it("loads previous page when clicked to previous button", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst);
            setup();
            const previousLink = await screen.findByText("< previous");
            fireEvent.click(previousLink);
            const firstPageUser = await screen.findByText("display1@user1");
            expect(firstPageUser).toBeInTheDocument();
        });
        it("displays error message when loading other page fails", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockRejectedValueOnce(mockFailGet);
            setup();
            const previousLink = await screen.findByText("< previous");
            fireEvent.click(previousLink);

            const errorMessage = await screen.findByText("User load failed");
            expect(errorMessage).toBeInTheDocument();
        });
        it("hides error message when successfully loading other page", async () => {
            apiCall.listUsers = jest
                .fn()
                .mockResolvedValueOnce(mockSuccessGetMultiPageLast)
                .mockRejectedValueOnce(mockFailGet)
                .mockResolvedValueOnce(mockSuccessGetMultiPageFirst);
            setup();
            const previousLink = await screen.findByText("< previous");
            fireEvent.click(previousLink);
            await screen.findByText("User load failed");
            fireEvent.click(previousLink);
            const errorMessage = await screen.findByText("User load failed");
            expect(errorMessage).not.toBeInTheDocument();
        });
    });
});

console.error = () => {
};
