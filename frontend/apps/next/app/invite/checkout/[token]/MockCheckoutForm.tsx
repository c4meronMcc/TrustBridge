'use client';
import { useState } from 'react';

interface MockCheckoutFormProps {
    amount: string;
    symbol: string;
    jobToken: string;
    paymentRequestId: string;
}

export default function MockCheckoutForm({ amount, symbol, jobToken, paymentRequestId }: MockCheckoutFormProps) {
    const [isLoading, setIsLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);

    const confirm = async () => {
        setIsLoading(true);
        const res = await fetch(`http://localhost:8080/api/v1/mock/payments/${paymentRequestId}/confirm`, {
            method: 'POST',
            credentials: 'include',
        });
        if (res.ok) {
            window.location.href = `http://localhost:3000/invite/success/${jobToken}`;
        } else {
            setMessage('Mock payment failed to confirm.');
        }
        setIsLoading(false);
    };

    const simulateFailure = async () => {
        setIsLoading(true);
        await fetch(`http://localhost:8080/api/v1/mock/payments/${paymentRequestId}/fail`, {
            method: 'POST',
            credentials: 'include'
        });
        setMessage('Simulated payment failure.');
        setIsLoading(false);
    };

    return (
        <div className="space-y-4">
            <div className="border-2 border-dashed border-amber-300 bg-amber-50 rounded-2xl p-5 text-[13px] text-amber-800">
                🧪 Mock payment provider active — no real charge will occur.
            </div>
            <button
                disabled={isLoading}
                onClick={confirm}
                className="w-full py-4 bg-[#3FCD6B] hover:bg-[#35bd60] rounded-xl text-[15px] font-semibold text-[#0F5525]"
            >
                {isLoading ? 'Processing…' : `Simulate Payment • ${symbol}${amount}`}
            </button>
            <button
                disabled={isLoading}
                onClick={simulateFailure}
                className="w-full py-3 bg-white border border-red-200 text-red-600 rounded-xl text-[13px] font-medium"
            >
                Simulate Failure
            </button>
            {message && <p className="text-[13px] text-red-600">{message}</p>}
        </div>
    );
}
