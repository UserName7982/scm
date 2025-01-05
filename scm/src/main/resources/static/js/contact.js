console.log("contact.js");
const base="localhost:8082";
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
        const data = await (await fetch(`http://${base}/api/contacts/${id}`)).json();
        console.log(data);
        document.querySelector("#contact_name").innerHTML = data.name;
        document.querySelector("#contact_phone").innerHTML=data.phoneno;
        document.querySelector("#contact_image").src = data.picture;
        document.querySelector("#contact_address").innerHTML=data.address;
        document.querySelector("#contact_description").innerHTML = data.discreption;
        if(data.favourite){
            document.querySelector("#contact_rating").innerHTML="favorite";
        }else{
            document.querySelector("#contact_rating").innerHTML="";
        }
        
        // Ensure this selector points to a valid element
        document.querySelector("#contact_email").innerHTML = data.email;

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
          const url=`http://${base}/user/contacts/delete/`+id;
            window.location.replace(url);
        }
      });
}
