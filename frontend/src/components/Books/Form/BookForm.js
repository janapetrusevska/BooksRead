import React, { useEffect, useState } from "react";
import FormField from "./BookFormField";
import ToggleSwitch from "./ToggleSwitch";
import {addOrUpdateBook, fetchGenres} from "../../Service/AxiosService";

const BookForm = ({ book }) => {
    const [genres, setGenres] = useState([]);
    const [cover, setCover] = useState(null);
    const [useUrl, setUseUrl] = useState(false);
    const [bookCover, setBookCover] = useState(null);
    const isEditMode = Boolean(book);

    const [formData, setFormData] = useState({
        title: '',
        author: '',
        language: '',
        rating: '',
        stars: '',
        points: '',
        readDate: '',
        genre: '',
        cover: '',
        status: '',
        note: ''
    });

    const statuses = ["READ", "READING", "WISHLIST"];
    const token = localStorage.getItem("token");

    useEffect(() => {
        const fetch = async () => {
            try {
                const genresData = await fetchGenres(token);
                setGenres(genresData);
            } catch (error) {
                console.log(error);
            }
        };
        fetch();
    }, [token]);

    useEffect(() => {
        if (book) {
            setFormData({
                title: book.title || '',
                author: book.author || '',
                language: book.language || '',
                rating: book.rating || '',
                stars: book.stars || '',
                points: book.points || '',
                readDate: book.readDate || '',
                genre: book.genre || '',
                cover: book.cover || '',
                status: book.status || '',
                note: book.note || ''
            });
            setBookCover(book.coverUrl);
        }
    }, [book]);

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setCover(file);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevData) => ({
            ...prevData,
            [name]: value,
        }));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        const bookDtoRequest = {
            title: formData.title,
            author: formData.author,
            language: formData.language,
            rating: formData.rating,
            stars: formData.stars,
            points: formData.points,
            readDate: formData.readDate,
            genre: formData.genre,
            status: formData.status,
            note: formData.note
        };

        if (useUrl && formData.cover) {
            bookDtoRequest.cover = formData.cover;
        } else if (!useUrl && cover) {
            bookDtoRequest.cover = cover;
        } else if (isEditMode && !cover && !formData.cover) {
            bookDtoRequest.cover = book.cover;
        }

        const data = new FormData();
        data.append('bookDtoRequest', JSON.stringify(bookDtoRequest));

        if (!useUrl && cover) {
            data.append('cover', cover);
        }

        try {
            await addOrUpdateBook(bookDtoRequest, cover, token, isEditMode, book?.id);
            window.location.reload();
        } catch (error) {
            console.log(error);
        }
    };



    return (
        <form onSubmit={handleSubmit} className="modal-form-container">
            <div className="left-column">
                <FormField label="Title" name="title" type="text" value={formData.title} required onChange={handleChange} />
                <FormField label="Author" name="author" type="text" value={formData.author} required onChange={handleChange} />
                <FormField label="Language" name="language" type="text" value={formData.language} onChange={handleChange} />
                <FormField label="Rating" name="rating" type="number" value={formData.rating} min="1" max="5"
                           onChange={handleChange} placeholder={"You can enter a rating between 1 and 5."}/>
                <FormField label="Stars" name="stars" type="number" value={formData.stars} required min="1" max="5"
                           onChange={handleChange} placeholder={"You can enter between 1 and 5 stars."}/>
                <FormField label="Points" name="points" type="number" value={formData.points} required min="1" max="3"
                           onChange={handleChange} placeholder={"You can give a book 1,2 or 3 points."}/>
            </div>
            <div className="right-column">
                <FormField label="Date" name="readDate" type="date" value={formData.readDate} onChange={handleChange} />
                <FormField label="Genre" name="genre" type="select" required options={genres} value={formData.genre} onChange={handleChange} />
                <FormField label="Status" name="status" type="select" options={statuses} value={formData.status} required onChange={handleChange} />
                {useUrl ? (
                    <FormField label="Cover URL" name="cover" type="text" value={formData.cover} onChange={handleChange} bookCover={bookCover} />
                ) : (
                    <FormField label="Cover File" name="cover" type="file" onChange={handleFileChange} bookCover={bookCover}/>
                )}
                <ToggleSwitch Name="Cover" onChange={() => setUseUrl(!useUrl)} />
                <FormField label="Note" name="note" type="textarea" rows={5} cols={37} value={formData.note} onChange={handleChange} />
            </div>
            <button type="submit" className="form-button">
                <b>{isEditMode ? "UPDATE BOOK" : "ADD BOOK"}</b>
            </button>
        </form>
    );
}

export default BookForm;