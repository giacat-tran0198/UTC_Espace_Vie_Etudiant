import React from "react";

const Input = (props) => {
    let inputClassName = "form-control" + (props.type === "file" ? "-file" : "") + (props.hasError === undefined ? "" : props.hasError ? " is-invalid" : " is-valid");
    return (
        <div>
            {props.label && <label>{props.label}</label>}
            <input
                name={props.name}
                className={inputClassName}
                type={props.type || "text"}
                placeholder={props.placeholder}
                value={props.value}
                onChange={props.onChange}
            />
            {props.hasError && <span className="invalid-feedback">{props.error}</span>}
        </div>
    )
}
Input.defaultProps = {
    onChange: () => {
    }
}
export default Input;