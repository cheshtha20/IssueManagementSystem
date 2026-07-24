import { PieChart, Pie, Cell, ResponsiveContainer, Label } from 'recharts';

export default function ProgressPieChart({ percent }) {
  const roundedPercent = Math.min(100, Math.max(0, Math.round(percent || 0)));
  const data = [
    { name: 'Completed', value: roundedPercent },
    { name: 'Remaining', value: 100 - roundedPercent },
  ];

  const COLORS = ['#10b981', '#e2e8f0']; // Green vs light grey

  return (
    <div className="progress-pie-container" style={{ width: '100%', height: 220, position: 'relative' }}>
      <ResponsiveContainer width="100%" height="100%">
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={65}
            outerRadius={80}
            startAngle={90}
            endAngle={-270}
            dataKey="value"
          >
            <Cell fill={COLORS[0]} />
            <Cell fill={COLORS[1]} />
            <Label
              value={`${roundedPercent}%`}
              position="center"
              fill="#0f172a"
              style={{ fontSize: '28px', fontWeight: 'bold', fontFamily: 'Inter, sans-serif' }}
            />
          </Pie>
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
}
