import React from "react";
import ReactDOM from "react-dom";
import {HashRouter} from "react-router-dom";
import {Provider} from "react-redux";

import "./index.css";
import reportWebVitals from "./reportWebVitals";
import App from "./containers/App";
import {configureStore} from "./redux/configureStore";


const store = configureStore();

ReactDOM.render(
    <React.StrictMode>
        <Provider store={store}>
            <HashRouter>
                <App/>
            </HashRouter>
        </Provider>
    </React.StrictMode>,
    document.getElementById("root")
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
