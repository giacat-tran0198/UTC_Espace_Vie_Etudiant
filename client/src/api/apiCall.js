import axios from "axios";
import {API_1_0_DISCUSSION, API_1_0_LOGIN, API_1_0_USER} from "../utils/constant";

export const signup = (user) => axios.post(API_1_0_USER, user);

export const login = (user) => axios.post(API_1_0_LOGIN, {}, {auth: user});

export const setAuthorizationHeader = ({username, password, isLoggedIn}) => {
    if (isLoggedIn) {
        axios.defaults.headers.common["Authorization"] = `Basic ${btoa(
            username + ":" + password
        )}`;
    } else {
        delete axios.defaults.headers.common["Authorization"];
    }
};

export const listUsers = (param = {page: 0, size: 3}) => {
    const path = `${API_1_0_USER}?page=${param.page || 0}&size=${param.size || 3}`;
    return axios.get(path);
};

export const getUser = (username) => axios.get(`${API_1_0_USER}/${username}`);

export const updateUser = (userId, body) => axios.put(`${API_1_0_USER}/${userId}`, body);

export const postDiscussion = (discussion) => axios.post(API_1_0_DISCUSSION, discussion);

export const loadDiscussions = (username) => {
    const basePath = username
        ? `${API_1_0_USER}/${username}/discussions`
        : API_1_0_DISCUSSION;
    return axios.get(basePath + "?page=0&size=5&sort=id,desc");
};

export const loadOldDiscussions = (discussionId, username) => {
    const basePath = username
        ? `${API_1_0_USER}/${username}/discussions`
        : API_1_0_DISCUSSION;
    const path = `${basePath}/${discussionId}?direction=before&page=0&size=5&sort=id,desc`;
    return axios.get(path);
};

export const loadNewDiscussions = (discussionId, username) => {
    const basePath = username
        ? `${API_1_0_USER}/${username}/discussions`
        : API_1_0_DISCUSSION;
    const path = `${basePath}/${discussionId}?direction=after&sort=id,desc`;
    return axios.get(path);
};

export const loadNewDiscussionCount = (discussionId, username) => {
    const basePath = username
        ? `${API_1_0_USER}/${username}/discussions`
        : API_1_0_DISCUSSION;
    const path = `${basePath}/${discussionId}?direction=after&count=true`;
    return axios.get(path);
};

export const postDiscussionFile = (file) => axios.post(API_1_0_DISCUSSION + "/upload", file);

export const deleteDiscussion = (discussionId) => axios.delete(API_1_0_DISCUSSION + "/" + discussionId);