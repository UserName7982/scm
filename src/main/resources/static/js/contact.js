console.log("contact.js");
const base = ""; // same origin as the page (works locally and on any deployment)
const contactModal = document.getElementById('view_contact_modal');
const options = {
    placement: 'bottom-right',
    backdrop: 'dynamic',
    backdropClasses: 'bg-gray-900/50 dark:bg-gray-900/80 fixed inset-0 z-40',
    closable: true,
    onHide: () => {
        console.log('modal is hidden');
    },
    onShow: () => {
        console.log('modal is shown');
    },
    onToggle: () => {
        console.log('modal has been toggled');
    },
};

const instanceOptions = {
    id: 'view_contact_modal',
    override: true
};
const viewcontactmodel = new Modal(contactModal, options, instanceOptions);

function opencontactmodal() {
    viewcontactmodel.show();
}

function closemodal() {
    viewcontactmodel.hide();
}

async function loaddata(id) {
    console.log(id);
    try {
        const data = await (await fetch(`${base}/api/contacts/${id}`)).json();
        console.log(data);
        // textContent (not innerHTML) so contact data can never inject HTML/JS
        const setText = (sel, val) => { document.querySelector(sel).textContent = val || ""; };
        setText("#contact_name", data.name);
        setText("#contact_phone", data.phoneno);
        setText("#contact_address", data.address);
        setText("#contact_description", data.discreption);
        setText("#contact_email", data.email);
        setText("#contact_rating", data.favourite ? "favorite" : "");
        document.querySelector("#contact_image").src = data.picture ||
            "https://toppng.com/uploads/preview/instagram-default-profile-picture-11562973083brycehrmyv.png";

        opencontactmodal();
    } catch (error) {
        console.log("error", error);
    }
}
async function deleteContact(id){
    Swal.fire({
        title: "Are you sure?",
        text: "You won't be able to revert this!",
        icon: "warning",
        showCancelButton: true,
        confirmButtonColor: "#3085d6",
        cancelButtonColor: "#d33",
        confirmButtonText: "Yes, delete it!"
      }).then((result) => {
        if (result.isConfirmed) {
          const url=`${base}/user/contacts/delete/`+id;
            window.location.replace(url);
        }
      });
}
