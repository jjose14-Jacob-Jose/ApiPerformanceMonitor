const INDEX_RESULTS_TABLE_CALL_ID = 0;
const JAVA_PARAMETER_CALLER_TIME_STAMP = "callerTimestampUTC";

function stringIsEmpty(string) {
    if (string.trim() === STRING_EMPTY) {
        return true;
    } else {
        return false;
    }

}

/**
 * Event-listener for 'Log Message' button.
 */
function makeAPILog() {

    // Adding Google reCaptcha token.
    let recaptchaToken = getReCaptchaToken();
    if (recaptchaToken === MSG_FAIL) {
        // Invalid Google reCaptcha.
        printAsAlert(MSG_TOO_MANY_REQUESTS);
        return;
    }
    const callerMessage = document.getElementById(HTML_ID_TEXTAREA_API_CALL_MESSAGE).value;
    const callerName = document.getElementById(HTML_ID_TEXT_API_CALL_APPLICATION_NAME).value;
    const callerTimestamp = document.getElementById(HTML_ID_DATETIME_API_CALL_TIMESTAMP).value;

    if (stringIsEmpty(callerMessage)) {
        printAsAlert(MSG_INPUT_MESSAGE_EMPTY)
        return;
    }
    if (stringIsEmpty(callerName)) {
        printAsAlert(MSG_INPUT_Name_EMPTY)
        return;
    }

    // Append the APM user's username to the caller-name parameter.
    let usernameFromCookie = getCookieValue(ID_BACKEND_COOKIE_USERNAME);
    if (usernameFromCookie === STRING_EMPTY) {
        usernameFromCookie = MSG_COOKIES_DISABLED_USERNAME;
        printAsAlert(MSG_COOKIES_DISABLED_ALERT);
    }

    const apiCall = {
        [JSON_REQUEST_KEY_API_CALL_CALLER_MESSAGE]: callerMessage,
        [JSON_REQUEST_KEY_API_CALL_CALLER_NAME]: callerName,
        [JSON_REQUEST_KEY_API_CALL_CALLER_TIMESTAMP_UTC]: callerTimestamp
    };

    const apmUser = {
        [JSON_REQUEST_KEY_GOOGLE_RECAPTCHA_TOKEN]: recaptchaToken,
        [JSON_REQUEST_KEY_USERNAME]: usernameFromCookie, // You should replace this with the actual username
        [JSON_REQUEST_KEY_API_CALL]: apiCall
    };



    fetch(URL_POST_API_CALL_FROM_APM_DASHBOARD, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(apmUser)
    })
        .then(response => {
            if (response.ok) {
                callerMessage.value = STRING_EMPTY; // Clear the textarea
                callerName.value = STRING_EMPTY; // Clear the textarea
                // Fetching and rendering the table.
                main();
            } else {
                alert(MSG_FAIL);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('An error occurred while sending the POST request.');
        });
}


/**
 * Display JSON as a table on the specified div.
 * Table headers are dynamically created.
 * @param {Array} jsonData - Response from API.
 * @param {string} idDiv - ID of HTML div where the table is to be rendered.
 * @param {string} idHtmlTable - ID of HTML table that will be created.
 */
function displayJSONAsTableOnDiv(jsonData, idDiv, idHtmlTable, cssClassHTMLTable) {
    const container = document.getElementById(idDiv);

    // Clear existing content in the container
    // container.innerHTML = '';

    if (jsonData.length === 0) {
        // Handle the case when there is no data to display
        container.textContent = 'No data available';
        return;
    }

    // Create a table element
    const table = document.createElement('table');
    table.setAttribute('id', idHtmlTable);
    table.setAttribute('class', cssClassHTMLTable);

    // Create an array of unique keys from the JSON objects
    const keys = Array.from(new Set(jsonData.flatMap(item => Object.keys(item))));

    // Create an array to store filter input elements
    const filterInputs = [];

    // Create table headers based on the unique keys and add filtering inputs
    const thead = document.createElement('thead');

    const headerRow = document.createElement('tr');
    keys.forEach(key => {
        const headerCell = document.createElement('th');
        headerCell.textContent = key;

        // Create an input element for filtering
        const filterInput = document.createElement('input');
        filterInput.type = 'text';
        filterInput.placeholder = `Filter ${key}`;

        // Attach an input event listener for filtering
        filterInput.addEventListener('input', () => {
            filterTable(idHtmlTable, key, filterInput.value, keys); // Pass keys as a parameter
        });

        headerCell.appendChild(filterInput);
        filterInputs.push({ key, input: filterInput });

        headerRow.appendChild(headerCell);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // Create table body and populate it with data
    const tbody = document.createElement('tbody');
    jsonData.forEach(item => {
        const row = document.createElement('tr');
        keys.forEach(key => {
            const cell = document.createElement('td');

            // Create a div element to wrap the content
            const div = document.createElement('div');

            // Format timestamp in ISO to local format.
            if(key === JAVA_PARAMETER_CALLER_TIME_STAMP)
                div.textContent = convertTimestampToLocalFormat(item[key]);
            else
                div.textContent = item[key] || ''; // Handle undefined values

            cell.appendChild(div);
            row.appendChild(cell);
        });
        tbody.appendChild(row);
    });
    table.appendChild(tbody);

    // Append the table to the container
    container.appendChild(table);

    // Id column is hidden using CSS styles.

}

/**
 * Clears a div and deletes all tables inside it.
 * @param idDiv : ID of the HTML div.
 */
function clearDivAndDeleteTables(idDiv) {
    try {
        const div = document.getElementById(idDiv);
        const tables = div.getElementsByTagName('table');
        // Remove each table individually
        while (tables.length > 0) {
            const table = tables[0];
            table.remove();
        }
        // Clear the div content
        div.innerHTML = '';
    } catch (error) {
        // Handle any potential errors here
        console.error('clearDivAndDeleteTables('+idDiv+'): ', error);
    }
}

/**
 *  Adding text-field filters to HTML table's header.
 * @param idHtmlTable : ID of HTML table.
 * @param columnKey
 * @param filterText : Text for filtering data.
 * @param keys
 */
function filterTable(idHtmlTable, columnKey, filterText, keys) { // Pass keys as a parameter
    const table = document.getElementById(idHtmlTable);
    const tbody = table.querySelector('tbody');
    const rows = tbody.getElementsByTagName('tr');
    const filterTextLower = filterText.toLowerCase();

    for (let i = 0; i < rows.length; i++) {
        const row = rows[i];
        const cell = row.querySelector(`td:nth-child(${keys.indexOf(columnKey) + 1})`);

        if (cell) {
            const cellText = cell.textContent.toLowerCase();
            try {
                if (cellText.includes(filterTextLower)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            } catch (error) {
                // Handle any errors here
                console.error('Error:', error);
            }
        }
    }
}

function fetchDataAndDisplayTableUsingPOST(url, idDiv, idTable, cssClassForTable, jsonDataInRequestBody ) {
    clearDivAndDeleteTables(idDiv);
    // Fetch the JSON data from your REST API using POST.
    getPostData(url, jsonDataInRequestBody,
        responseJSONData => {
            displayJSONAsTableOnDiv(responseJSONData, idDiv, idTable, cssClassForTable)
        },
        error => {
            console.error('Error while making a POST request to ' + url + ' Error: ', error);
        }
    );
}

/**
 * Convert the date range to ISO format.
 *
 */
function formatDateTimeRangeToISOFormat() {
    // Get the datetime-local input values
    const dateTimeRangeStartHTMLFormat = document.getElementById(HTML_ID_DATETIME_RANGE_START);
    const dateTimeRangeEndHTMLFormat = document.getElementById(HTML_ID_DATETIME_RANGE_END);

    // Check if both inputs have valid values
    if (dateTimeRangeStartHTMLFormat.value && dateTimeRangeEndHTMLFormat.value) {
        // Convert JavaScript Date objects to timestamps (milliseconds since Unix epoch)
        const dateTimeRangeStartString = new Date(dateTimeRangeStartHTMLFormat.value).toISOString();
        const dateTimeRangeEndString = new Date(dateTimeRangeEndHTMLFormat.value).toISOString();

        // Creating JavaScript object.
        // Name of object should be same as Model class in Java.
        const RequestForDateRange = {
            dateTimeRangeStartString : dateTimeRangeStartString,
            dateTimeRangeEndString : dateTimeRangeEndString
        };

        // Converting JavaScript object to JSON.
        const jsonForRequestBody = JSON.stringify(RequestForDateRange);

        fetchDataAndDisplayTableUsingPOST(URL_GET_API_CALLS_WITHIN_DATE_TIME_RANGE, HTML_ID_DIV_RESPONSE_FROM_ALL_API_CALLS, HTML_ID_TABLE_RESPONSE_FROM_ALL_API_CALLS, HTML_CSS_CLASS_TABLE_RESPONSE_FROM_ALL_API_CALLs, jsonForRequestBody);


    } else {
        // Handle the case when one or both inputs are empty
        printAsAlert('Enter starting and ending range.');
    }
}

/**
 *  Show an Alert.
 * @param message : string to be shown on Alert window.
 */
function printAsAlert(message) {
    alert(message);
}

/**
 * Clear contents of the input fields to log a new API call.
 */
function clearFormContents() {
    document.getElementById(HTML_ID_TEXTAREA_API_CALL_MESSAGE).value = STRING_EMPTY;
    document.getElementById(HTML_ID_TEXT_API_CALL_APPLICATION_NAME).value = STRING_EMPTY;
    document.getElementById(HTML_ID_DATETIME_API_CALL_TIMESTAMP).value = STRING_EMPTY;

    // Calling 'main' to display the original table again.
    // main();
}

/**
 * Clear date-time fields of range selection.
 */
function btnApplyDateTimeRangeClear() {
    document.getElementById(HTML_ID_DATETIME_RANGE_START).value = STRING_EMPTY;
    document.getElementById(HTML_ID_DATETIME_RANGE_END).value = STRING_EMPTY;

    // Calling 'main' to display the original table again.
    main();
}

// Logout. Clear session.
function logoutUser() {
    // Clear all cookies.
    clearAllCookies();

    fetch("/auth/logout", {
        method: "GET"
    })
        .then(response => {
            if (response.status === 200) {
                // Redirect to your login or home page
                window.location.href = "/login"; // Adjust the URL as needed
            } else {
                // Handle the error
                console.error("Logout failed");
            }
        })
        .catch(error => console.error(error));
}

/**
 * Converts a timestamp in UTC timezone to local time.
 * @param isoTimestamp timestamp in UTC
 * @returns {string} local time zone in the format 'dd-MMM-yyyy hh:mm am/pm'
 */
function convertTimestampToLocalFormat(isoTimestamp) {
    const date = new Date(isoTimestamp); // Parse the ISO timestamp
    const options = {
        year: 'numeric',
        month: 'short',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: true,
        weekday: 'short'
    };

    // Format the date and time
    return date.toLocaleString(undefined, options);
}


/**
 * Methods are run when the HTML method is loaded.
 *
 */
function main() {
    clearFormContents();
    fetchDataAndDisplayTableUsingPOST(URL_GET_API_CALLS_ALL, HTML_ID_DIV_RESPONSE_FROM_ALL_API_CALLS, HTML_ID_TABLE_RESPONSE_FROM_ALL_API_CALLS, HTML_CSS_CLASS_TABLE_RESPONSE_FROM_ALL_API_CALLs);
}

/**
 * Specify functions to be executed when the documented is loaded.
 */
document.addEventListener('DOMContentLoaded', main);