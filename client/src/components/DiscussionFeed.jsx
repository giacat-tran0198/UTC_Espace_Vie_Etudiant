import React, {Component} from "react";
import * as apiCall from "../api/apiCall";
import Spinner from "./Spinner";
import DiscussionView from "./DiscussionView";
import Modal from "./Modal";

class DiscussionFeed extends Component {
    state = {
        page: {
            content: []
        },
        isLoadingDiscussions: false,
        newDiscussionCount: 0,
        isLoadingOldDiscussions: false,
        isLoadingNewDiscussions: false,
        isDeletingDiscussion: false,
    };

    componentDidMount() {
        this.setState({isLoadingDiscussions: true});
        apiCall.loadDiscussions(this.props.user)
            .then((response) =>
                this.setState({page: response.data, isLoadingDiscussions: false},
                    () => (this.counter = setInterval(this.checkCount, 3000))
                )
            );
    }

    componentWillUnmount() {
        clearInterval(this.counter);
    }

    checkCount = () => {
        const discussions = this.state.page.content;
        let topDiscussionId = discussions.length === 0 ? 0 : discussions[0].id;
        apiCall.loadNewDiscussionCount(topDiscussionId, this.props.user)
            .then((response) => this.setState({newDiscussionCount: response.data.count}));
    }

    onClickLoadMore = () => {
        const discussions = this.state.page.content;
        if (discussions.length === 0) return;
        const discussionAtBottom = discussions[discussions.length - 1];
        this.setState({isLoadingOldDiscussions: true});
        apiCall
            .loadOldDiscussions(discussionAtBottom.id, this.props.user)
            .then((response) => {
                const page = {...this.state.page};
                page.content = [...page.content, ...response.data.content];
                page.last = response.data.last;
                this.setState({page, isLoadingOldDiscussions: false});
            })
            .catch((error) => this.setState({isLoadingOldDiscussions: false}));
    };

    onClickLoadNew = () => {
        const discussions = this.state.page.content;
        let topDiscussionId = discussions.length === 0 ? 0 : discussions[0].id;
        this.setState({isLoadingNewDiscussions: true});
        apiCall
            .loadNewDiscussions(topDiscussionId, this.props.user)
            .then((response) => {
                const page = {...this.state.page};
                page.content = [...response.data, ...page.content];
                this.setState({page, newDiscussionCount: 0, isLoadingNewDiscussions: false});
            })
            .catch((error) => {
                this.setState({isLoadingNewDiscussions: false});
            });
    };

    onClickDeleteDiscussion = (discussion) => {
        this.setState({ discussionToBeDeleted: discussion });
    };

    onClickModalCancel = () => {
        this.setState({ discussionToBeDeleted: undefined });
    };

    onClickModalOk = () => {
        this.setState({ isDeletingDiscussion: true });
        apiCall.deleteDiscussion(this.state.discussionToBeDeleted.id).then((response) => {
            const page = { ...this.state.page };
            page.content = page.content.filter(
                (discussion) => discussion.id !== this.state.discussionToBeDeleted.id
            );
            this.setState({
                discussionToBeDeleted: undefined,
                page,
                isDeletingDiscussion: false,
            });
        });
    };

    render() {

        if (this.state.isLoadingDiscussions) {
            return <Spinner/>;
        }
        if (this.state.page.content.length === 0 && this.state.newDiscussionCount === 0) {
            return (
                <div className="card card-header text-center">There are no discussions</div>
            );
        }

        const newDiscussionCountMessage =
            this.state.newDiscussionCount === 1
                ? "There is 1 new discussion"
                : `There are ${this.state.newDiscussionCount} new discussions`;

        return (
            <div>
                {this.state.newDiscussionCount > 0 && (
                    <div
                        className="card card-header text-center"
                        onClick={!this.state.isLoadingNewDiscussions && this.onClickLoadNew}
                        style={{cursor: this.state.isLoadingNewDiscussions ? "not-allowed" : "pointer"}}
                    >
                        {this.state.isLoadingNewDiscussions ? <Spinner/> : newDiscussionCountMessage}
                    </div>
                )}
                {this.state.page.content.map((discussion) => (
                    <DiscussionView key={discussion.id} discussion={discussion} onClickDelete={() => this.onClickDeleteDiscussion(discussion)}/>)
                )}
                {this.state.page.last === false && (
                    <div
                        className="card card-header text-center"
                        onClick={!this.state.isLoadingOldDiscussions && this.onClickLoadMore}
                        style={{cursor: this.state.isLoadingOldDiscussions ? "not-allowed" : "pointer"}}
                    >
                        {this.state.isLoadingOldDiscussions ? <Spinner/> : "Load More"}
                    </div>
                )}
                <Modal
                    visible={this.state.discussionToBeDeleted && true}
                    onClickCancel={this.onClickModalCancel}
                    body={
                        this.state.discussionToBeDeleted &&
                        `Are you sure to delete "${this.state.discussionToBeDeleted.content}"?`
                    }
                    title="Delete!"
                    okButton="Delete Discussion"
                    onClickOk={this.onClickModalOk}
                    pendingApiCall={this.state.isDeletingDiscussion}
                />
            </div>
        );
    }
}

export default DiscussionFeed;
