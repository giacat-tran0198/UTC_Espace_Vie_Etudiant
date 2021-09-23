import axios from "axios";
import * as apiCall from "./apiCall";
import {API_1_0_DISCUSSION, API_1_0_LOGIN, API_1_0_USER} from "../utils/constant";

describe("apiCall", () => {
    describe("signup", () => {
        it("calls " + API_1_0_USER, () => {
            const mockSignup = jest.fn();
            axios.post = mockSignup;
            apiCall.signup();

            const path = mockSignup.mock.calls[0][0];
            expect(path).toBe(API_1_0_USER);
        });
    });
    describe("login", () => {
        it("calls " + API_1_0_LOGIN, () => {
            const mockLogin = jest.fn();
            axios.post = mockLogin;
            apiCall.login({username: "test-user", password: "P4ssword"});
            const path = mockLogin.mock.calls[0][0];
            expect(path).toBe(API_1_0_LOGIN);
        });
    });
    describe("listUser", () => {
        it("calls /api/1.0/users?page=0&size=3 when no param provided for listUsers", () => {
            const mockListUsers = jest.fn();
            axios.get = mockListUsers;
            apiCall.listUsers();
            expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=0&size=3");
        });
        it("calls /api/1.0/users?page=5&size=10 when corresponding params provided for listUsers", () => {
            const mockListUsers = jest.fn();
            axios.get = mockListUsers;
            apiCall.listUsers({page: 5, size: 10});
            expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=5&size=10");
        });
        it("calls /api/1.0/users?page=5&size=3 when only page param provided for listUsers", () => {
            const mockListUsers = jest.fn();
            axios.get = mockListUsers;
            apiCall.listUsers({page: 5});
            expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=5&size=3");
        });
        it("calls /api/1.0/users?page=0&size=5 when only size param provided for listUsers", () => {
            const mockListUsers = jest.fn();
            axios.get = mockListUsers;
            apiCall.listUsers({size: 5});
            expect(mockListUsers).toBeCalledWith("/api/1.0/users?page=0&size=5");
        });
    });
    describe("getUser", () => {
        it("calls /api/1.0/users/user5 when user5 is provided for getUser", () => {
            const mockGetUser = jest.fn();
            axios.get = mockGetUser;
            apiCall.getUser("user5");
            expect(mockGetUser).toBeCalledWith("/api/1.0/users/user5");
        });
    });
    describe("updateUser", () => {
        it("calls /api/1.0/users/5 when 5 is provided for updateUser", () => {
            const mockUpdateUser = jest.fn();
            axios.put = mockUpdateUser;
            apiCall.updateUser("5");
            const path = mockUpdateUser.mock.calls[0][0];
            expect(path).toBe("/api/1.0/users/5");
        });
    });
    describe("postDiscussion", () => {
        it("calls /api/1.0/discussion", () => {
            const mockPostDiscussion = jest.fn();
            axios.post = mockPostDiscussion;
            apiCall.postDiscussion();
            const path = mockPostDiscussion.mock.calls[0][0];
            expect(path).toBe(API_1_0_DISCUSSION);
        });
    });
    describe("loadDiscussions", () => {
        it("calls /api/1.0/discussions?page=0&size=5&sort=id,desc when no param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadDiscussions();
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/discussions?page=0&size=5&sort=id,desc"
            );
        });
        it("calls /api/1.0/users/user1/discussions?page=0&size=5&sort=id,desc when user param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadDiscussions("user1");
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/users/user1/discussions?page=0&size=5&sort=id,desc"
            );
        });
    });
    describe("loadOldDiscussions", () => {
        it("calls /api/1.0/discussions/5?direction=before&page=0&size=5&sort=id,desc when discussion id param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadOldDiscussions(5);
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/discussions/5?direction=before&page=0&size=5&sort=id,desc"
            );
        });
        it("calls /api/1.0/users/user3/discussions/5?direction=before&page=0&size=5&sort=id,desc when discussion id and username param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadOldDiscussions(5, "user3");
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/users/user3/discussions/5?direction=before&page=0&size=5&sort=id,desc"
            );
        });
    });
    describe("loadNewDiscussions", () => {
        it("calls /api/1.0/discussions/5?direction=after&sort=id,desc when discussion id param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadNewDiscussions(5);
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/discussions/5?direction=after&sort=id,desc"
            );
        });
        it("calls /api/1.0/users/user3/discussions/5?direction=after&sort=id,desc when discussion id and username param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadNewDiscussions(5, "user3");
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/users/user3/discussions/5?direction=after&sort=id,desc"
            );
        });
    });
    describe("loadNewDiscussionCount", () => {
        it("calls /api/1.0/discussions/5?direction=after&count=true when discussion id param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadNewDiscussionCount(5);
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/discussions/5?direction=after&count=true"
            );
        });
        it("calls /api/1.0/users/user3/discussions/5?direction=after&count=true when discussion id and username param provided", () => {
            const mockGetDiscussions = jest.fn();
            axios.get = mockGetDiscussions;
            apiCall.loadNewDiscussionCount(5, "user3");
            expect(mockGetDiscussions).toBeCalledWith(
                "/api/1.0/users/user3/discussions/5?direction=after&count=true"
            );
        });
    });
    describe("postDiscussionFile", () => {
        it("calls /api/1.0/discussions/upload", () => {
            const mockPostDiscussionFile = jest.fn();
            axios.post = mockPostDiscussionFile;
            apiCall.postDiscussionFile();
            const path = mockPostDiscussionFile.mock.calls[0][0];
            expect(path).toBe(API_1_0_DISCUSSION + "/upload");
        });
    });
    describe("deleteDiscussion", () => {
        it("calls /api/1.0/discussions/5 when discussion id param provided as 5", () => {
            const mockDelete = jest.fn();
            axios.delete = mockDelete;
            apiCall.deleteDiscussion(5);
            const path = mockDelete.mock.calls[0][0];
            expect(path).toBe(API_1_0_DISCUSSION + "/5");
        });
    });
});