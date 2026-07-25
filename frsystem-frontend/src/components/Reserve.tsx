import React, { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { reservationService } from '../services/ReservationService';

interface ReserveProps {
    isOpen: boolean;
    onClose: () => void;
    onSuccess: () => void;
    flight: any | null;
}

interface SeatRow {
    rowLetter: string;
    rowSeats: string[];
}

const Reserve: React.FC<ReserveProps> = ({ isOpen, onClose, onSuccess, flight }) => {
    const [selectedSeat, setSelectedSeat] = useState<string>('');
    const [loading, setLoading] = useState<boolean>(false);
    const [occupiedSeats, setOccupiedSeats] = useState<string[]>([]);
    
    // SEATMAP FOR BETTER UI
    const generateSeatMap = (): SeatRow[] => {
        if (!flight || !flight.airplane) return [];
        
        const capacity = flight.airplane.capacity;
        const seatsPerRow = 10; // ROW CAPACITY (10 FOR NOW)
        const numRows = Math.ceil(capacity / seatsPerRow);
        const seatMap: SeatRow[] = [];

        for (let i = 0; i < numRows; i++) {
            // USING ASCII FOR ADDITION
            const rowLetter = String.fromCharCode(65 + i); 
            const rowSeats: string[] = [];
            
            for (let j = 1; j <= seatsPerRow; j++) {
                const seatIndex = i * seatsPerRow + j;
                if (seatIndex <= capacity) {
                    rowSeats.push(`${rowLetter}${j}`); 
                }
            }
            seatMap.push({ rowLetter, rowSeats });
        }
        return seatMap;
    };

    const handleSeatClick = (seat: string) => {
        setSelectedSeat(seat);
    };

    const handleReserve = async () => {
        if (!selectedSeat) {
            toast.warning("Select a seat!");
            return;
        }

        setLoading(true);
        try {
            const payload = {
                flightId: flight.id,
                seatNumber: selectedSeat
            };
            
            await reservationService.createReservation(payload);
            
            toast.success(`Seat ${selectedSeat} has been reserved!`);
            onSuccess(); 
        } catch (error: any) {
            console.error("Reserve Error:", error);
            const errorData = error.response?.data;
            let errorMessage = "error";
            
            if (typeof errorData === 'string') {
                errorMessage = errorData;
            } else if (errorData && errorData.message) {
                errorMessage = errorData.message;
            }
            
            toast.error(errorMessage);
        } finally {
            setLoading(false);
        }
    };
    
    useEffect(() => {
        if (isOpen && flight) {
            const fetchOccupiedSeats = async () => {
                try {
                    const data: any[] = await reservationService.getOccupiedSeats(flight.id);
                    const bookedSeats = data.map(res => res.seatNumber);
                    setOccupiedSeats(bookedSeats);
                } catch (error) {
                    console.error("Failed to fetch occupied seats:", error);
                }
            };
            fetchOccupiedSeats();
        } else {
            setSelectedSeat('');
            setOccupiedSeats([]);
        }
    }, [isOpen, flight]);

    if (!isOpen || !flight) return null;

    const seatMap = generateSeatMap();

    return (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
            <div className="bg-white rounded-xl shadow-2xl w-full max-w-4xl flex flex-col max-h-[90vh]">
                
                <div className="bg-red-900 text-white p-5 rounded-t-xl flex justify-between items-center">
                    <div>
                        <h2 className="text-xl font-bold">{flight.flightNumber}</h2>
                        <p className="text-sm text-indigo-200">
                            {flight.departureAirport?.iataCode} ➔ {flight.arrivalAirport?.iataCode}
                        </p>
                    </div>
                    <button onClick={onClose} className="text-red-200 hover:text-white text-3xl">&times;</button>
                </div>

                {/* SEATMAP */}
                <div className="p-6 overflow-y-auto bg-gray-200 flex-1 flex items-center justify-center gap-8">
                    <div className="max-w-2xl space-y-3">
                        {seatMap.map((row, index) => (
                            <div key={index} className="flex items-center justify-center gap-4">
                                {/* SEATS */}
                                <div className="flex gap-2">
                                    {row.rowSeats.map((seat) => {
                                        const isOccupied = occupiedSeats.includes(seat);
                                        const isSelected = selectedSeat === seat;
                                        
                                        return (
                                            <button
                                                key={seat}
                                                type="button"
                                                onClick={() => !isOccupied && handleSeatClick(seat)} 
                                                disabled={isOccupied} 
                                                className={`w-10 h-10 md:w-12 md:h-12 rounded-t-lg rounded-b-sm font-semibold text-xs transition flex items-center justify-center shadow-sm border-2
                                                    ${isOccupied 
                                                        ? 'bg-gray-400 text-white border-gray-500 opacity-70' 
                                                        : isSelected 
                                                            ? 'bg-red-500 text-white border-red-600 scale-110 shadow-md' 
                                                            : 'bg-white text-gray-700 border-gray-300 hover:bg-red-100 hover:border-red-400' 
                                                    }
                                                `}
                                            >
                                                {seat}
                                            </button>
                                        );
                                    })}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
                    
                <div className="flex items-center gap-4 justify-end p-2">
                    <button 
                        onClick={handleReserve}
                        disabled={!selectedSeat || loading}
                        className="bg-red-600 text-white px-6 py-2.5 rounded-lg font-bold hover:bg-red-700 disabled:bg-gray-400 transition shadow-md"
                    >
                        {loading ? 'Loading...' : 'Reserve'}
                    </button>
                </div>
            </div>
        </div>
    );
};

export default Reserve;