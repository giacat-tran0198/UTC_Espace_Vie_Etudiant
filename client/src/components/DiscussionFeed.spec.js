import React from "react";
import {fireEvent, render, screen, waitFor} from "@testing-library/react";
import {MemoryRouter} from "react-router-dom";
import * as apiCall from "../api/apiCall";
import DiscussionFeed from "./DiscussionFeed";

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

const originalSetInterval = window.setInterval;
const originalClearInterval = window.clearInterval;

let timedFunction;

const useFakeIntervals = () => {
    window.setInterval = (callback, interval) => (timedFunction = callback);
    window.clearInterval = () => (timedFunction = undefined);
};

const useRealIntervals = () => {
    window.setInterval = originalSetInterval;
    window.clearInterval = originalClearInterval;
};

const runTimer = () => (timedFunction && timedFunction());

const setup = (props, state = loggedInStateUser1) => {
    const store = createStore(authReducer, state);
    return render(
        <Provider store={store}>
            <MemoryRouter>
                <DiscussionFeed {...props} />
            </MemoryRouter>
        </Provider>
    );
};

const mockEmptyResponse = {
    data: {
        content: []
    }
};

const mockSuccessGetNewDiscussionsList = {
    data: [
        {
            id: 21,
            content: "This is the newest discussion",
            date: 1561294668539,
            user: {
                id: 1,
                username: "user1",
                displayName: "display1",
                image: "profile1.png"
            }
        }
    ]
};

const mockSuccessGetDiscussionsMiddleOfMultiPage = {
    data: {
        content: [
            {
                id: 5,
                content: "This discussion is in middle page",
                date: 1561294668539,
                user: {
                    id: 1,
                    username: "user1",
                    displayName: "display1",
                    image: "profile1.png"
                }
            }
        ],
        number: 0,
        first: false,
        last: false,
        size: 5,
        totalPages: 2
    }
};

const mockSuccessGetDiscussionsSinglePage = {
    data: {
        content: [
            {
                id: 10,
                content: "This is the latest discussion",
                date: 1561294668539,
                user: {
                    id: 1,
                    username: "user1",
                    displayName: "display1",
                    image: "profile1.png"
                }
            }
        ],
        number: 0,
        first: true,
        last: true,
        size: 5,
        totalPages: 1
    }
};

const mockSuccessGetDiscussionsFirstOfMultiPage = {
    data: {
        content: [
            {
                id: 10,
                content: "This is the latest discussion",
                date: 1561294668539,
                user: {
                    id: 1,
                    username: "user1",
                    displayName: "display1",
                    image: "profile1.png"
                }
            },
            {
                id: 9,
                content: "This is discussion 9",
                date: 1561294668539,
                user: {
                    id: 1,
                    username: "user1",
                    displayName: "display1",
                    image: "profile1.png"
                }
            },
        ],
        number: 0,
        first: true,
        last: false,
        size: 5,
        totalPages: 2
    }
};

const mockSuccessGetDiscussionsLastOfMultiPage = {
    data: {
        content: [
            {
                id: 1,
                content: "This is the oldest discussion",
                date: 1561294668539,
                user: {
                    id: 1,
                    username: "user1",
                    displayName: "display1",
                    image: "profile1.png"
                }
            }
        ],
        number: 0,
        first: true,
        last: true,
        size: 5,
        totalPages: 2
    }
};

describe("DiscussionFeed", () => {
    describe("Lifecycle", () => {
        it("calls loadDiscussions when it is rendered", () => {
            apiCall.loadDiscussions = jest.fn().mockResolvedValue(mockEmptyResponse);
            setup();
            expect(apiCall.loadDiscussions).toHaveBeenCalled();
        });
        it("calls loadDiscussions with user parameter when it is rendered with user property", () => {
            apiCall.loadDiscussions = jest.fn().mockResolvedValue(mockEmptyResponse);
            setup({user: "user1"});
            expect(apiCall.loadDiscussions).toHaveBeenCalledWith("user1");
        });
        it("calls loadDiscussions without user parameter when it is rendered without user property", () => {
            apiCall.loadDiscussions = jest.fn().mockResolvedValue(mockEmptyResponse);
            setup();
            const parameter = apiCall.loadDiscussions.mock.calls[0][0];
            expect(parameter).toBeUndefined();
        });
        it("calls loadNewDiscussionCount with topDiscussion id", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            setup();
            await waitFor(runTimer);
            await screen.findByText("There is 1 new discussion", {}, {timeout: 2000});
            const firstParam = apiCall.loadNewDiscussionCount.mock.calls[0][0];
            expect(firstParam).toBe(10);
            useRealIntervals();
        });
        it("calls loadNewDiscussionCount with topDiscussion id and username when rendered with user property", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            setup({user: "user1"});
            await waitFor(runTimer);
            await screen.findByText("There is 1 new discussion");
            expect(apiCall.loadNewDiscussionCount).toHaveBeenCalledWith(10, "user1");
            useRealIntervals();
        });
        it("displays new discussion count as 1 after loadNewDiscussionCount success", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            expect(newDiscussionCount).toBeInTheDocument();
            useRealIntervals();
        });
        it("displays new discussion count constantly", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            setup({user: "user1"});
            await waitFor(runTimer);
            await screen.findByText("There is 1 new discussion");
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 2}});
            runTimer();
            const newDiscussionCount = await screen.findByText("There are 2 new discussions");
            expect(newDiscussionCount).toBeInTheDocument();
            useRealIntervals();
        });
        // it("does not call loadNewDiscussionCount after component is unmounted", async () => {
        //     useFakeIntervals();
        //     apiCall.loadDiscussions = jest
        //         .fn()
        //         .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
        //     apiCall.loadNewDiscussionCount = jest
        //         .fn()
        //         .mockResolvedValue({data: {count: 1}});
        //     const {unmount} = setup({user: "user1"});
        //     await waitFor(runTimer);
        //     await screen.findByText("There is 1 new discussion");
        //     unmount();
        //     expect(apiCall.loadNewDiscussionCount).toHaveBeenCalledTimes(1);
        //     useRealIntervals();
        // });
        it("displays new discussion count as 1 after loadNewDiscussionCount success when user does not have discussions initially", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest.fn().mockResolvedValue(mockEmptyResponse);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            expect(newDiscussionCount).toBeInTheDocument();
            useRealIntervals();
        });
    });
    describe("Layout", () => {
        it("displays no discussion message when the response has empty page", async () => {
            apiCall.loadDiscussions = jest.fn().mockResolvedValue(mockEmptyResponse);
            setup();
            const message = await screen.findByText("There are no discussions");
            expect(message).toBeInTheDocument();
        });
        it("does not display no discussion message when the response has page of discussion", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsSinglePage);
            const {queryByText} = setup();
            await waitFor(() => expect(queryByText("There are no discussions")).not.toBeInTheDocument());
        });
        it("displays spinner when loading the discussions", async () => {
            apiCall.loadDiscussions = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve(mockSuccessGetDiscussionsSinglePage);
                    }, 300);
                });
            });
            const {queryByText} = setup();
            expect(queryByText("Loading...")).toBeInTheDocument();
        });
        it("displays discussion content", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsSinglePage);
            setup();
            const discussionContent = await screen.findByText("This is the latest discussion");
            expect(discussionContent).toBeInTheDocument();
        });
        it("displays Load More when there are next pages", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            setup();
            const loadMore = await screen.findByText("Load More");
            expect(loadMore).toBeInTheDocument();
        });
    });
    describe("Interactions", () => {
        it("calls loadOldDiscussions with discussion id when clicking Load More", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsLastOfMultiPage);
            setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            const firstParam = apiCall.loadOldDiscussions.mock.calls[0][0];
            expect(firstParam).toBe(9);
        });
        it("calls loadOldDiscussions with discussion id and username when clicking Load More when rendered with user property", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsLastOfMultiPage);
            setup({user: "user1"});
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            expect(apiCall.loadOldDiscussions).toHaveBeenCalledWith(9, "user1");
        });
        it("displays loaded old discussion when loadOldDiscussions api call success", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsLastOfMultiPage);
            setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            const oldDiscussion = await screen.findByText("This is the oldest discussion");
            expect(oldDiscussion).toBeInTheDocument();
        });
        it("hides Load More when loadOldDiscussions api call returns last page", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsLastOfMultiPage);
            const {queryByText} = setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            await screen.findByText("This is the oldest discussion");
            expect(queryByText("Load More")).not.toBeInTheDocument();
        });
        // load new discussions
        it("calls loadNewDiscussions with discussion id when clicking New Discussion Count Card", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            setup();
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            const firstParam = apiCall.loadNewDiscussions.mock.calls[0][0];
            expect(firstParam).toBe(10);
            useRealIntervals();
        });
        it("calls loadNewDiscussions with discussion id and username when clicking New Discussion Count Card", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            expect(apiCall.loadNewDiscussions).toHaveBeenCalledWith(10, "user1");
            useRealIntervals();
        });
        it("displays loaded new discussion when loadNewDiscussions api call success", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            const newDiscussion = await screen.findByText("This is the newest discussion");
            expect(newDiscussion).toBeInTheDocument();
            useRealIntervals();
        });
        it("hides new discussion count when loadNewDiscussions api call success", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            const {queryByText} = setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            await screen.findByText("This is the newest discussion");
            expect(queryByText("There is 1 new discussion")).not.toBeInTheDocument();
            useRealIntervals();
        });
        it("does not allow loadOldDiscussions to be called when there is an active api call about it", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsLastOfMultiPage);
            setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            // fireEvent.click(loadMore);
            expect(apiCall.loadOldDiscussions).toHaveBeenCalledTimes(1);
        });
        it("replaces Load More with spinner when there is an active api call about it", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve(mockSuccessGetDiscussionsLastOfMultiPage);
                    }, 300);
                });
            });
            const {queryByText} = setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            const spinner = await screen.findByText("Loading...");
            expect(spinner).toBeInTheDocument();
            expect(queryByText("Load More")).not.toBeInTheDocument();
        });
        it("replaces Spinner with Load More after active api call for loadOldDiscussions finishes with middle page response", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest.fn().mockImplementation(() =>
                new Promise((resolve, reject) =>
                    setTimeout(() => resolve(mockSuccessGetDiscussionsMiddleOfMultiPage), 300)
                )
            );
            const {queryByText} = setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            await screen.findByText("This discussion is in middle page");
            expect(queryByText("Loading...")).not.toBeInTheDocument();
            expect(queryByText("Load More")).toBeInTheDocument();
        });
        it("replaces Spinner with Load More after active api call for loadOldDiscussions finishes error", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadOldDiscussions = jest.fn().mockImplementation(
                () => new Promise((resolve, reject) =>
                    setTimeout(() =>
                        reject({response: {data: {}}}), 300)
                )
            );
            const {queryByText} = setup();
            const loadMore = await screen.findByText("Load More");
            fireEvent.click(loadMore);
            await screen.findByText("Loading...");

            await waitFor(() => expect(queryByText("Loading...")).not.toBeInTheDocument());
            await waitFor(() => expect(queryByText("Load More")).toBeInTheDocument());
        });
        // loadNewDiscussions

        it("does not allow loadNewDiscussions to be called when there is an active api call about it", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            const {queryByText} = setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            // fireEvent.click(newDiscussionCount);
            expect(apiCall.loadNewDiscussions).toHaveBeenCalledTimes(1);
            useRealIntervals();
        });
        it("replaces There is 1 new discussion with spinner when there is an active api call about it", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest.fn().mockImplementation(() =>
                new Promise((resolve, reject) =>
                    setTimeout(() => resolve(mockSuccessGetNewDiscussionsList), 300)
                )
            );
            const {queryByText} = setup();
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            const spinner = await screen.findByText("Loading...");
            expect(spinner).toBeInTheDocument();
            expect(queryByText("There is 1 new discussion")).not.toBeInTheDocument();
            useRealIntervals();
        });
        it("removes Spinner and There is 1 new discussion after active api call for loadNewDiscussions finishes with success", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetNewDiscussionsList);
            const {queryByText} = setup({user: "user1"});
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            await screen.findByText("This is the newest discussion");
            expect(queryByText("Loading...")).not.toBeInTheDocument();
            expect(queryByText("There is 1 new discussion")).not.toBeInTheDocument();
            useRealIntervals();
        });
        it("replaces Spinner with There is 1 new discussion after active api call for loadNewDiscussions fails", async () => {
            useFakeIntervals();
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({data: {count: 1}});
            apiCall.loadNewDiscussions = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        reject({response: {data: {}}});
                    }, 300);
                });
            });
            const {queryByText} = setup();
            await waitFor(runTimer);
            const newDiscussionCount = await screen.findByText("There is 1 new discussion");
            fireEvent.click(newDiscussionCount);
            await screen.findByText("Loading...");

            await waitFor(() => expect(queryByText("Loading...")).not.toBeInTheDocument());
            await waitFor(() => expect(queryByText("There is 1 new discussion")).toBeInTheDocument());
            useRealIntervals();
        });
        it("displays modal when clicking delete on discussion", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });
            const { queryByTestId, container } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const modalRootDiv = queryByTestId("modal-root");
                expect(modalRootDiv).toHaveClass("modal fade d-block show");
            });
        });
        it("hides modal when clicking cancel", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });
            const { queryByTestId, container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                fireEvent.click(queryByText("Cancel"));
                const modalRootDiv = queryByTestId("modal-root");
                expect(modalRootDiv).not.toHaveClass("d-block show");
            });
        });
        it("displays modal with information about the action", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const message = queryByText(
                    `Are you sure to delete "This is the latest discussion"?`
                );
                expect(message).toBeInTheDocument();
            });
        });
        it("calls deleteDiscussion api with discussion id when delete button is clicked on modal", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockResolvedValue({});
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);
                expect(apiCall.deleteDiscussion).toHaveBeenCalledWith(10);
            });
        });
        it("hides modal after successful deleteDiscussion api call", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockResolvedValue({});
            const { container, queryByText, queryByTestId } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);    
            });
            await waitFor(()=>{
                const modalRootDiv = queryByTestId("modal-root");
                expect(modalRootDiv).not.toHaveClass("d-block show");    
            });
            
        });
        it("removes the deleted discussion from document after successful deleteDiscussion api call", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockResolvedValue({});
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);    
            });
            
            await waitFor(()=>{
                const deletedDiscussionContent = queryByText("This is the latest discussion");
                expect(deletedDiscussionContent).not.toBeInTheDocument();    
            });
            
        });
        it("disables Modal Buttons when api call in progress", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);

                expect(deleteDiscussionButton).toBeDisabled();
                expect(queryByText("Cancel")).toBeDisabled();    
            });
        });
        it("displays spinner when api call in progress", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);
                const spinner = queryByText("Loading...");
                expect(spinner).toBeInTheDocument();    
            });
        });
        it("hides spinner when api call finishes", async () => {
            apiCall.loadDiscussions = jest
                .fn()
                .mockResolvedValue(mockSuccessGetDiscussionsFirstOfMultiPage);
            apiCall.loadNewDiscussionCount = jest
                .fn()
                .mockResolvedValue({ data: { count: 1 } });

            apiCall.deleteDiscussion = jest.fn().mockImplementation(() => {
                return new Promise((resolve, reject) => {
                    setTimeout(() => {
                        resolve({});
                    }, 300);
                });
            });
            const { container, queryByText } = setup();
            await waitFor(()=>{
                const deleteButton = container.querySelectorAll("button")[0];
                fireEvent.click(deleteButton);
                const deleteDiscussionButton = queryByText("Delete Discussion");
                fireEvent.click(deleteDiscussionButton);
            });
            await waitFor(()=>{
                const spinner = queryByText("Loading...");
                expect(spinner).not.toBeInTheDocument();
            });
        });
    });
});

console.error = () => {
};