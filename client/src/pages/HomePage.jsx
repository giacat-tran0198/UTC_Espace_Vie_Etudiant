import React, {Component} from "react";
import {connect} from "react-redux";
import UserList from "../components/UserList";
import DiscussionSubmit from "../components/DiscussionSubmit";
import DiscussionFeed from "../components/DiscussionFeed";

class HomePage extends Component {
    render() {
        return (
            <div data-testid="homepage">
                <div className="row">
                    <div className="col-8">
                        {this.props.loggedInUser.isLoggedIn && <DiscussionSubmit/>}
                        <DiscussionFeed/>
                    </div>
                    <div className="col-4">
                        <UserList/>
                    </div>
                </div>
            </div>
        );
    }
}

const mapStateToProps = (state) => ({
    loggedInUser: state
});

export default connect(mapStateToProps)(HomePage);