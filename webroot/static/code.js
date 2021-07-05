const alert = document.querySelector('#alert');

let servicesRequest = new Request('/service');

populate()

function populate() {
    fetch(servicesRequest)
        .then(function (response) {
            return response.json();
        })
        .then(function (serviceList) {
            const tableContainer = document.querySelector('#service-list');
            tableContainer.innerHTML = ""
            serviceList.forEach(service => {
                const deleteBtn = document.createElement("button")
                deleteBtn.setAttribute("id", "delete-service")
                deleteBtn.setAttribute("type", "button")
                deleteBtn.setAttribute("class", "btn-close")
                deleteBtn.setAttribute("aria-label", "Close")
                deleteBtn.onclick = evt => {
                    fetch(`/service?name=${service.name}`, {
                        method: 'delete',
                        headers: {
                            'Accept': 'application/json, text/plain, */*',
                            'Content-Type': 'application/json'
                        }
                    }).then(res => {
                        if (res.ok) {
                            return res.text()
                        }
                        throw new Error('Problem when try to delete service')
                    }).then(res => {
                        alert.setAttribute("class", "alert alert-success")
                        alert.setAttribute("style", "style='visibility: visible'")
                        alert.textContent = "Delete service successfully"
                        populate()
                    }).catch(error => {
                        alert.setAttribute("class", "alert alert-danger")
                        alert.setAttribute("style", "style='visibility: visible'")
                        alert.textContent = "Sorry! Something wrong happen...please contact the admin"
                        console.log('error', error)
                    });
                }

                const tr = document.createElement("tr");
                const tdName = document.createElement("td");
                const tdDate = document.createElement("td");
                const tdStatus = document.createElement("td");
                const tdDelete = document.createElement("td");
                tdName.appendChild(document.createTextNode(service.name));
                tdDate.appendChild(document.createTextNode(service.addedDttm));
                tdStatus.appendChild(document.createTextNode(service.status));
                tdDelete.appendChild(deleteBtn);
                tr.appendChild(tdName);
                tr.appendChild(tdDate);
                tr.appendChild(tdStatus);
                tr.appendChild(tdDelete)
                tableContainer.appendChild(tr);
            });
        });
}


const refreshButton = document.querySelector('#refresh');
const updateButton = document.querySelector('#update-service');
const saveButton = document.querySelector('#post-service');

refreshButton.onclick = evt => {
    alert.setAttribute("class", "")
    alert.setAttribute("style", "style='visibility: hidden'")
    location.reload();
}

saveButton.onclick = evt => {
    let urlName = document.querySelector('#url-name').value;
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name: urlName})
    }).then(res => {
        if (res.ok) {
            return res.text()
        }
        throw new Error('Problem when try to add new service')
    }).then(res => {
        alert.setAttribute("class", "alert alert-success")
        alert.setAttribute("style", "style='visibility: visible'")
        alert.textContent = "Added new service successfully"
        populate()
    }).catch(error => {
        alert.setAttribute("class", "alert alert-danger")
        alert.setAttribute("style", "style='visibility: visible'")
        alert.textContent = "Sorry! Something wrong happen...please contact the admin"
        console.log('error', error)
    });
}

updateButton.onclick = evt => {
    let urlOld = document.querySelector('#url-name-old').value;
    let urlNew = document.querySelector('#url-name-new').value;
    fetch(`/service?name=${urlOld}`, {
        method: 'put',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({name: urlNew})
    }).then(res => {
        if (res.ok) {
            return res.text()
        }
        throw new Error('Problem when try to update new service')
    }).then(res => {
        alert.setAttribute("class", "alert alert-success")
        alert.setAttribute("style", "style='visibility: visible'")
        alert.textContent = "Updated service successfully"
        populate()
    }).catch(error => {
        alert.setAttribute("class", "alert alert-danger")
        alert.setAttribute("style", "style='visibility: visible'")
        alert.textContent = "Sorry! Something wrong happen...please contact the admin"
        console.log('error', error)
    });
}
