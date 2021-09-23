import React, {Component} from "react";
import {format} from "timeago.js";
import {Link} from "react-router-dom";
import {connect} from 'react-redux';
import ProfileImageWithDefault from "./ProfileImageWithDefault";

class DiscussionView extends Component {
    render() {
        const {discussion, onClickDelete} = this.props;
        const {user, date} = discussion;
        const {username, displayName, image} = user;
        const relativeDate = format(date);
        const attachmentImageVisible = discussion.attachment && discussion.attachment.fileType.startsWith('image');
        const ownedByLoggedInUser = user.id === this.props.loggedInUser.id;

        return (
            <div className="card p-1">
                <div className="d-flex">
                    <ProfileImageWithDefault
                        className="rounded-circle m-1"
                        width="32"
                        height="32"
                        image={image}
                    />
                    <div className="flex-fill m-auto pl-2">
                        <Link to={`/${username}`} className="list-group-item-action">
                            <h6 className="d-inline">
                                {displayName}@{username}
                            </h6>
                        </Link>
                        <span className="text-black-50"> - </span>
                        <span className="text-black-50">{relativeDate}</span>
                    </div>
                    {ownedByLoggedInUser && (
                        <button className="btn btn-outline-danger btn-sm" onClick={onClickDelete}>
                            <i className="far fa-trash-alt"/>
                        </button>
                    )}
                </div>
                <div className="pl-5">{discussion.content}</div>
                {attachmentImageVisible && (
                    <div className="pl-5">
                        <img
                            alt="attachment"
                            src={`/images/attachments/${discussion.attachment.name}`}
                            className="img-fluid"
                        />
                    </div>
                )}
            </div>
        );
    }
}

const mapStateToProps = (state) => ({
    loggedInUser: state
});

export default connect(mapStateToProps)(DiscussionView);